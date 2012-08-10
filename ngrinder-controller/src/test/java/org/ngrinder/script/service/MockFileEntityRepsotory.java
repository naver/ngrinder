package org.ngrinder.script.service;

import java.io.File;

import org.ngrinder.model.User;
import org.ngrinder.script.repository.FileEntityRepository;
import org.springframework.stereotype.Component;

@Component
public class MockFileEntityRepsotory extends FileEntityRepository {
	
	private File userRepoDir;

	public File getUserRepoDirectory(User user) {
		return userRepoDir;
	}

	public void setUserRepository(File userRepository) {
		this.userRepoDir = userRepository;
	}
}
