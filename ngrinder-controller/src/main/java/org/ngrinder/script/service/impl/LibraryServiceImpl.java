/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.ngrinder.script.service.impl;

import java.util.List;

import org.ngrinder.script.model.Library;
import org.ngrinder.script.service.LibraryService;
import org.ngrinder.script.util.LibraryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

	@Autowired
	private LibraryUtil libraryUtil;

	@Override
	public void saveLibrary(Library library) {
		libraryUtil.createLibraryPath();
		libraryUtil.saveLibraryFile(library);
	}

	@Override
	public void deleteLibrary(String libraryName) {
		libraryUtil.deleteLibraryFile(libraryName);
	}

	@Override
	public List<Library> getLibraries() {
		return libraryUtil.getLibrary();
	}
}
