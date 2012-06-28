package com.nhncorp.ngrinder.script.service;

import java.util.List;

import com.nhncorp.ngrinder.script.model.Library;

public interface LibraryService {

	List<Library> getLibraries();

	void saveLibrary(Library library);

	void deleteLibrary(String libraryName);

}
