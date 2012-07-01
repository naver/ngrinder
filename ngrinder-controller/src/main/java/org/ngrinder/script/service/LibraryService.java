package org.ngrinder.script.service;

import java.util.List;

import org.ngrinder.script.model.Library;


public interface LibraryService {

	List<Library> getLibraries();

	void saveLibrary(Library library);

	void deleteLibrary(String libraryName);

}
