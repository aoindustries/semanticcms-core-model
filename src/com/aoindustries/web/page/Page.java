/*
 * ao-web-page - Java API for modeling web page content and relationships.
 * Copyright (C) 2013, 2014, 2015, 2016  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of ao-web-page.
 *
 * ao-web-page is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ao-web-page is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ao-web-page.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aoindustries.web.page;

import com.aoindustries.lang.NotImplementedException;
import com.aoindustries.util.AoCollections;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Page extends Node {

	public static final int MIN_TOC_LEVELS = 1;

	/**
	 * This matches the table of contents implementation in toc.inc.jsp
	 * and is a result of HTML only having H1 through H6.  H1 is reserved
	 * for the page title and headings become H2 through H6.
	 */
	public static final int MAX_TOC_LEVELS = 5;

	public static final int DEFAULT_TOC_LEVELS = 3;

	private PageRef pageRef;
	private PageRef src;
	private String title;
	private Boolean toc;
	private int tocLevels = DEFAULT_TOC_LEVELS;
	private List<PageRef> parentPages;
	private boolean allowParentMismatch;
	private List<PageRef> childPages;
	private boolean allowChildMismatch;
	private List<Element> elements;
	private Map<String,Element> elementsById;

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Page)) return false;
		return pageRef.equals(((Page)obj).pageRef);
	}

	@Override
	public int hashCode() {
		return pageRef.hashCode();
	}

	@Override
	public Page freeze() {
		if(
			elements != null
			&& (
				elementsById == null
				|| elements.size() > elementsById.size()
			)
		) {
			// Generate missing ids
			for(Element element : elements) {
				String id = element.getId();
				if(id == null) {
					StringBuilder possId = Element.generateIdPrefix(element.getLabel(), element.getDefaultIdPrefix());
					int possIdLen = possId.length();
					// Find an unused identifier
					for(int i=1; i<=Integer.MAX_VALUE; i++) {
						if(i == Integer.MAX_VALUE) throw new IllegalStateException("ID not generated");
						if(i>1) possId.append('-').append(i);
						String newId = possId.toString();
						if(
							elementsById == null
							|| !elementsById.containsKey(newId)
						) {
							element.setId(newId);
							element.freeze();
							break;
						}
						// Reset for next element number to check
						possId.setLength(possIdLen);
					}
				}
			}
			assert elementsById != null;
			assert elements.size() == elementsById.size() : "elements and elementsById are different size: " + elements.size() + " != " + elementsById.size();
		}
		super.freeze();
		return this;
	}

	/**
	 * The PageRef that refers to this page.
	 */
	public PageRef getPageRef() {
		return pageRef;
	}

	public void setPageRef(PageRef pageRef) {
		checkNotFrozen();
		this.pageRef = pageRef;
	}

	/**
	 * The PageRef of the editable source for this page, if any.
	 */
	public PageRef getSrc() {
		return src;
	}

	public void setSrc(PageRef src) {
		checkNotFrozen();
		this.src = src;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		checkNotFrozen();
        this.title = title;
    }

	/**
	 * Gets the table of contents (toc) setting:
	 * <ul>
	 *   <li>{@literal true} - Always show table of contents</li>
	 *   <li>{@literal false} - Never show table of contents</li>
	 *   <li>{@literal null} (The default) - Show table of contents depending on number of entries in the table</li>
	 * </ul>
	 */
	public Boolean getToc() {
		return toc;
	}

	public void setToc(Boolean toc) {
		checkNotFrozen();
		this.toc = toc;
	}

	public int getTocLevels() {
		return tocLevels;
	}

	public void setTocLevels(int tocLevels) {
		checkNotFrozen();
		if(tocLevels < MIN_TOC_LEVELS || tocLevels > MAX_TOC_LEVELS) throw new IllegalArgumentException("tocLevels must be between " + MIN_TOC_LEVELS + " and " + MAX_TOC_LEVELS + ": " + tocLevels);
		this.tocLevels = tocLevels;
	}

	public List<PageRef> getParentPages() {
		if(parentPages == null) {
			throw new NotImplementedException("TODO: Auto parent(s) here");
			//return Collections.emptyList();
		} else {
			return Collections.unmodifiableList(parentPages);
		}
	}

	public void addParentPage(PageRef parentPage) {
		checkNotFrozen();
		if(parentPages == null) parentPages = new ArrayList<>();
		parentPages.add(parentPage);
	}

	public boolean getAllowParentMismatch() {
		return allowParentMismatch;
	}

	public void setAllowParentMismatch(boolean allowParentMismatch) {
		checkNotFrozen();
		this.allowParentMismatch = allowParentMismatch;
	}

	public List<PageRef> getChildPages() {
		if(childPages == null) return Collections.emptyList();
		return Collections.unmodifiableList(childPages);
	}

	public void addChildPage(PageRef childPage) {
		checkNotFrozen();
		if(childPages == null) childPages = new ArrayList<>();
		childPages.add(childPage);
	}

	public boolean getAllowChildMismatch() {
		return allowChildMismatch;
	}

	public void setAllowChildMismatch(boolean allowChildMismatch) {
		checkNotFrozen();
		this.allowChildMismatch = allowChildMismatch;
	}

	/**
	 * Gets all elements in the page (including all child elements), in the
	 * order they were declared in the page.
	 */
	public List<Element> getElements() {
		if(elements == null) return Collections.emptyList();
		return Collections.unmodifiableList(elements);
	}

	/**
	 * Gets all elements in the page (including all child elements) that are of the
	 * given type, in the order they were declared in the page.
	 */
	public <E extends Element> List<E> filterElements(Class<E> clazz) {
		if(elements == null) return Collections.emptyList();
		return AoCollections.filter(elements, clazz);
	}

	/**
	 * Gets the elements indexed by id, in no particular order.
	 * Note, while the page is being created, elements with automatic IDs will not be in
	 * this map.  However, once frozen, every element will have an ID.
	 * 
	 * @see  #freeze()
	 */
	public Map<String,Element> getElementsById() {
		if(elementsById == null) return Collections.emptyMap();
		return Collections.unmodifiableMap(elementsById);
	}

	/**
	 * Adds an element to this page.
	 */
	public void addElement(Element element) {
		checkNotFrozen();
		element.setPage(this);
		// elements
		if(elements == null) elements = new ArrayList<>();
		elements.add(element);
		// elementsById
		addToElementsById(element);
	}

	private void addToElementsById(Element element) {
		String id = element.getId();
		if(id != null) {
			if(elementsById == null) elementsById = new HashMap<>();
			if(elementsById.put(id, element) != null) throw new AssertionError("Duplicate id: " + id);
		}
	}

	/**
	 * Called when an element within this page has its id set.
	 * This will only be called at most once, when the element's id goes from null to non-null.
	 *
	 * @see  Element#setId(java.lang.String)
	 */
	void onElementIdSet(Element element) {
		addToElementsById(element);
	}

	/**
	 * The page label is its title.
	 */
	@Override
	public String getLabel() {
		return title;
	}

	@Override
	public String getListItemCssClass() {
		return getChildPages().isEmpty() ? "ao-web-page-list-item-page-nochildren" : "ao-web-page-list-item-page-children";
	}

	// <editor-fold desc="Top Level Headings">
	private List<Heading> topLevelHeadings;

	public List<Heading> getTopLevelHeadings() {
		if(topLevelHeadings == null) return Collections.emptyList();
		return Collections.unmodifiableList(topLevelHeadings);
	}

	/**
	 * Adds a top level heading to this page.
	 */
	public void addTopLevelHeading(Heading heading) {
		checkNotFrozen();
		if(topLevelHeadings == null) topLevelHeadings = new ArrayList<>();
		topLevelHeadings.add(heading);
	}
	// </editor-fold>
}
