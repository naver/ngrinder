package org.ngrinder.script.service.impl;

import java.util.List;

import org.ngrinder.script.model.Library;
import org.ngrinder.script.service.LibraryService;
import org.ngrinder.script.util.LibraryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


/**
 * Library service implement
 * 
 * @author Tobi
 * @since
 * @date 2012-6-28
 */
@Service
public class LibraryServiceImpl implements LibraryService {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(LibraryServiceImpl.class);

	@Override
	public void saveLibrary(Library library) {
		LibraryUtil.createLibraryPath();
		LibraryUtil.saveLibraryFile(library);
	}

	@Override
	public void deleteLibrary(String libraryName) {
		LibraryUtil.deleteLibraryFile(libraryName);
	}

	@Override
	public List<Library> getLibraries() {
		return LibraryUtil.getLibrary();
	}
}
