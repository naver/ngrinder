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
package org.ngrinder.jnlp.impl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.ngrinder.common.util.CompressionUtil;
import org.ngrinder.jnlp.JNLPLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jnlp.JNLPClassLoader;

/**
 * {@link JNLPLoader} implementation.
 * 
 * @author maoyb
 */
public class JNLPLoaderImpl implements JNLPLoader {
	
	private static final Logger LOG = LoggerFactory.getLogger(JNLPLoaderImpl.class);

	private ClassLoader localClassLoader;

	public JNLPLoaderImpl() {
		localClassLoader = Thread.currentThread().getContextClassLoader();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ngrinder.jnlp.JNLPLoader#isWebStartPossible()
	 */
	@Override
	public boolean isWebStartPossible() {
		return (localClassLoader instanceof JNLPClassLoader);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ngrinder.jnlp.JNLPLoader#resolveRemoteJars(File jnlpLibPath)
	 */
	@Override
	public List<File> resolveRemoteJars(File jnlpLibPath) {

		List<File> fileString = new ArrayList<File>();

		JNLPClassLoader jnlpClassLoader = (JNLPClassLoader) localClassLoader;
		try {
			URL[] urls = jnlpClassLoader.getURLs();

			for (URL each : urls) {
				String jarName = FilenameUtils.getName(each.toString());
				JarFile jar = jnlpClassLoader.getJarFile(each);

				String jarLocalPath = jar.getName();
				File srcFile = new File(jarLocalPath);
				long srcFIleStamp = FileUtils.checksumCRC32(srcFile);
				File desFile = new File(jnlpLibPath, jarName);
				if (!desFile.exists() || (FileUtils.checksumCRC32(desFile) != srcFIleStamp)) {
					FileUtils.copyFile(srcFile, desFile);
				}
				if (jarName.equals("native.jar")) {
					CompressionUtil.unjar(desFile, jnlpLibPath.getAbsolutePath());
				}
				fileString.add(desFile);
			}
		} catch (IOException e) {
			LOG.error("Resolving remote jars error: {}", e.getMessage());
		}
		return fileString;
	}

}
