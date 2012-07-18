package org.ngrinder.script.service;

import java.io.File;

import org.ngrinder.model.User;
import org.ngrinder.script.repository.FileEntityRepository;
import org.springframework.stereotype.Component;

@Component
public class MockFileEntityRepsotory extends FileEntityRepository {
	private File userRepository;

	public File getUserRepository(User user) {
		return userRepository;
	}

	public void setUserRepository(File userRepository) {
		this.userRepository = userRepository;
	}
}
