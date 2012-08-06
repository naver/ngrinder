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
package org.ngrinder.perftest.model;

/**
 * Performance Test Status
 * 
 * @author JunHo Yoon
 * 
 */
public enum Status {
	/** test ready */
	READY,
	/** Just before starting console */
	START_CONSOLE,
	/** Just after staring console */
	START_CONSOLE_FINISHED,
	/** Just before starting agents */
	START_AGENTS,
	/** Just after starting agents */
	START_AGENTS_FINISHED,
	/** Just before distributing files */
	DISTRIBUTE_FILES,
	/** Just after distributing files */
	DISTRIBUTE_FILES_FINISHED,
	/** Just before staring testing */
	START_TESTING,
	TESTING,
	/** Just after staring testing */
	TESTING_FINISHED,
	/** Test finish */
	FINISHED,
	/** Stopped by error */
	STOP_ON_ERROR,
	/** Test cancel */
	CANCELED
}
