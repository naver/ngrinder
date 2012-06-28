package com.nhncorp.ngrinder.script.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.nhncorp.ngrinder.script.model.Library;
import com.nhncorp.ngrinder.script.service.LibraryService;
import com.nhncorp.ngrinder.script.util.LibraryUtil;

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
