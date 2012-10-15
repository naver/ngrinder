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
package org.ngrinder.monitor.controller.domain;

import javax.management.ObjectName;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import org.ngrinder.monitor.share.domain.MonitorInfo;

/**
 * 
 * Monitor collection info domain.
 *
 * @author Mavlarn
 * @since 2.0
 */
public class MonitorCollectionInfoDomain {
	private ObjectName objectName;
	private String attrName;
	@SuppressWarnings("unused")
	private Class<? extends MonitorInfo> resultClass;

	/**
	 * Constructor for the collection info.
	 * @param objectName is the object name related with JMX domain name
	 * @param attrName	is the attribute name in this domain, used to get concrete monitor data
	 * @param resultClass is the Class type of that monitor data with that attribute name
	 */
	public MonitorCollectionInfoDomain(ObjectName objectName, String attrName,
			Class<? extends MonitorInfo> resultClass) {
		this.objectName = objectName;
		this.attrName = attrName;
		this.resultClass = resultClass;
	}

	public ObjectName getObjectName() {
		return objectName;
	}

	public String getAttrName() {
		return attrName;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

}
