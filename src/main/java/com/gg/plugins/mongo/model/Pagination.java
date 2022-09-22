/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.model;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;

public class Pagination {

	@NotNull private final Collection<Runnable> mySetFilterListeners = new ArrayList<>();

	private NbPerPage nbPerPage;

	private int pageNumber;

	private int totalDocuments;

	public Pagination() {
		this.pageNumber = 1;
		this.nbPerPage = NbPerPage.ALL;
		this.totalDocuments = 0;
	}

	public void next() {
		setPageNumber(getPageNumber() + 1);
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
		for (Runnable listener : mySetFilterListeners) {
			listener.run();
		}
	}

	public void previous() {
		setPageNumber(getPageNumber() - 1);
	}

	public void setTotalDocuments(int totalDocuments) {
		this.totalDocuments = totalDocuments;
	}

	public int getStartIndex() {
		if (NbPerPage.ALL.equals(nbPerPage)) {
			return 0;
		} else {
			return getNbDocumentsPerPage() * (getPageNumber() - 1);
		}
	}

	public int getNbDocumentsPerPage() {
		return nbPerPage.nb;
	}

	public NbPerPage getNbPerPage() {
		return nbPerPage;
	}

	public void setNbPerPage(NbPerPage nbPerPage) {
		this.nbPerPage = nbPerPage;
		this.pageNumber = 1;
		for (Runnable listener : mySetFilterListeners) {
			listener.run();
		}
	}

	public void addSetPageListener(@NotNull Runnable runnable) {
		mySetFilterListeners.add(runnable);
	}

	public int getTotalPageNumber() {
		if (getNbDocumentsPerPage() == 0) {
			return 1;
		}
		return new BigDecimal(totalDocuments).divide(new BigDecimal(getNbDocumentsPerPage()), RoundingMode.CEILING)
		                                     .toBigInteger()
		                                     .intValue();
	}
}
