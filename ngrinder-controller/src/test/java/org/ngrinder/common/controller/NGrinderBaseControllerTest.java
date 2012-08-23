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
package org.ngrinder.common.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.ngrinder.model.User;
import org.ngrinder.perftest.controller.PerfTestController;
import org.ngrinder.perftest.service.AbstractPerfTestTransactionalTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Class description.
 *
 * @author Mavlarn
 * @since
 */
public class NGrinderBaseControllerTest extends AbstractPerfTestTransactionalTest {
	
	//NGrinderBaseController is not a component, use its sub-class to test.
	@Autowired
	private PerfTestController perfTestController;

	@Test
	public void testCurrentUser() {
		User currUser = perfTestController.currentUser();
		assertThat(currUser, notNullValue());
	}

	@Test
	public void testGetErrorMessages() {
		//in unit test, messageSource can not be resolved properly. Maybe there is a better way
		//to make in auto wired.
		MessageSource resources = new ClassPathXmlApplicationContext("servlet-context-message.xml");
		ReflectionTestUtils.setField(perfTestController, "messageSource", resources);
		String errMsg = perfTestController.getErrorMessages("startTest.scriptError");
		assertThat(errMsg, is("script is not found !"));
	}

}
