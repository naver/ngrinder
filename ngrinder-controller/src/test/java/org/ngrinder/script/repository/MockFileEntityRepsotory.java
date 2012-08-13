package org.ngrinder.script.repository;

import java.io.File;

import org.ngrinder.infra.annotation.TestOnlyComponent;
import org.ngrinder.model.User;
import org.ngrinder.script.repository.FileEntryRepository;

@TestOnlyComponent
public class MockFileEntityRepsotory extends FileEntryRepository {

	private File userRepoDir;

	@Override
	public File getUserRepoDirectory(User user) {
		return userRepoDir;
	}

	public void setUserRepository(File userRepository) {
		this.userRepoDir = userRepository;
	}
}
