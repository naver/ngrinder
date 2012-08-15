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
package org.ngrinder.dns;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * NameStore test class
 *
 * @author mavlarn
 * @Since 3.0
 */
public class NameStoreTest {

	@Test
	public void testNameStoreInit() {
		System.setProperty("ngrinder.etc.hosts", "1.1.1.1:aaa.com,2.2.2.2:bbb.com");
		NameStore.initFromSystemProperty();
		
		String ip = NameStore.getInstance().get("aaa.com");
		assertThat(ip, is("1.1.1.1"));
		
		NameStore.getInstance().remove("bbb.com");
		ip = NameStore.getInstance().get("bbb.com");
		assertThat(ip, nullValue());

	}

	@Test
	public void testNameStoreInitInvald() {
		System.setProperty("ngrinder.etc.hosts", "1.1.1.1,:bbb.com");
		NameStore.initFromSystemProperty();
		
		String ip = NameStore.getInstance().get("bbb.com");
		assertThat(ip, nullValue());
		
	}

	@Test
	public void testNameStoreInitEmpty() {
		System.setProperty("ngrinder.etc.hosts", "");
		NameStore.initFromSystemProperty();
		
		String ip = NameStore.getInstance().get("bbb.com");
		assertThat(ip, nullValue());
		
	}

}
