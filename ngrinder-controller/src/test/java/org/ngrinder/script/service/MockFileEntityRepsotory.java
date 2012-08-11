package org.ngrinder.script.service;

import java.io.File;

import org.ngrinder.model.User;
import org.ngrinder.script.repository.FileEntryRepository;
import org.springframework.stereotype.Component;

@Component
public class MockFileEntityRepsotory extends FileEntryRepository {
	
	private File userRepoDir;

	public File getUserRepoDirectory(User user) {
		return userRepoDir;
	}

	public void setUserRepository(File userRepository) {
		this.userRepoDir = userRepository;
	}
}
