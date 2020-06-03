package org.kohsuke.github;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Redefined for nGrinder.
 *
 * It has {@link org.kohsuke.github.GHTreeEntry#setSha(java.lang.String)}
 * for saving html url of github test script.
 *
 * @since 3.5.0
 */
public class GHTreeEntry {
	/* package almost final */GHTree tree;

	private String path, mode, type, sha, url;
	private long size;

	/**
	 * Get the path such as
	 * "subdir/file.txt"
	 *
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Get mode such as
	 * 100644
	 *
	 * @return the mode
	 */
	public String getMode() {
		return mode;
	}

	/**
	 * Gets the size of the file, such as
	 * 132
	 * @return The size of the path or 0 if it is a directory
	 */
	public long getSize() {
		return size;
	}

	/**
	 * Gets the type such as:
	 * "blob", "tree", etc.
	 *
	 * @return The type
	 */
	public String getType() {
		return type;
	}


	/**
	 * SHA1 of this object.
	 */
	public String getSha() {
		return sha;
	}

	public void setSha(String sha) {
		this.sha = sha;
	}

	/**
	 * API URL to this Git data, such as
	 * https://api.github.com/repos/jenkinsci
	 * /jenkins/git/commits/b72322675eb0114363a9a86e9ad5a170d1d07ac0
	 */
	public URL getUrl() {
		return GitHub.parseURL(url);
	}

	/**
	 * If this tree entry represents a file, then return its information.
	 * Otherwise null.
	 */
	public GHBlob asBlob() throws IOException {
		if (type.equals("blob"))
			return tree.repo.getBlob(sha);
		else
			return null;
	}

	/**
	 * If this tree entry represents a file, then return its content.
	 * Otherwise null.
	 */
	public InputStream readAsBlob() throws IOException {
		if (type.equals("blob"))
			return tree.repo.readBlob(sha);
		else
			return null;
	}

	/**
	 * If this tree entry represents a directory, then return it.
	 * Otherwise null.
	 */
	public GHTree asTree() throws IOException {
		if (type.equals("tree"))
			return tree.repo.getTree(sha);
		else
			return null;
	}
}
