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
package org.ngrinder.perftest.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import javax.annotation.PostConstruct;

import net.grinder.SingleConsole;

import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.springframework.stereotype.Component;

@Component
public class ConsoleManager {
	private Queue<ConsoleEntry> consoleQueue = new ArrayDeque<ConsoleEntry>();
	private List<ConsoleEntry> consoleInUse = new ArrayList<ConsoleEntry>();

	@PostConstruct
	public void init() {
		for (int i = 0; i < 20; i++) {
			consoleQueue.add(new ConsoleEntry(i + 12000));
		}
	}

	public SingleConsole getAvailableConsole() {
		ConsoleEntry consoleEntry = consoleQueue.poll();
		if (consoleEntry == null) {
			throw new NGrinderRuntimeException("no console entry available");
		}
		getConsoleInUse().add(consoleEntry);
		return new SingleConsole(consoleEntry.getPort());
	}

	public void returnBackConsole(SingleConsole console) {
		try {
			console.shutdown();
		} catch (Exception e) {
		}
		ConsoleEntry consoleEntry = new ConsoleEntry(console.getConsoleProperties().getConsolePort());
		consoleQueue.add(consoleEntry);
		getConsoleInUse().remove(consoleEntry);
	}

	public List<ConsoleEntry> getConsoleInUse() {
		return consoleInUse;
	}

}
