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
package org.ngrinder.model;

import java.util.Comparator;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;

/**
 * Tag class for categorization of {@link PerfTest}.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
@Entity
@Table(name = "TAG")
public class Tag extends BaseModel<Tag> implements Comparator<Tag>, Comparable<Tag> {

	/**
	 * UUID.
	 */
	private static final long serialVersionUID = -1;

	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "tags")
	private Set<PerfTest> perfTests;

	/**
	 * Tag value.
	 */
	private String tagValue;

	/**
	 * Default constructor.
	 */
	public Tag() {
	}

	/**
	 * Constructor.
	 * 
	 * @param tagValue
	 *            tag value
	 */
	public Tag(String tagValue) {
		this.tagValue = StringUtils.trimToEmpty(tagValue);

	}

	public String getTagValue() {
		return tagValue;
	}

	public void setTagValue(String tagValue) {
		this.tagValue = tagValue;
	}

	public Set<PerfTest> getPerfTests() {
		return perfTests;
	}

	public void setPerfTests(Set<PerfTest> perfTests) {
		this.perfTests = perfTests;
	}

	@Override
	public int compare(Tag o1, Tag o2) {
		return o1.tagValue.compareTo(o2.getTagValue());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Tag)) {
			return false;
		}
		return StringUtils.equalsIgnoreCase(this.getTagValue(), ((Tag) obj).getTagValue());
	}
	
	@Override
	public int hashCode() {
		return StringUtils.trimToEmpty(this.getTagValue()).hashCode();
	}

	@Override
	public int compareTo(Tag o) {
		return compare(this, o);
	}

}
