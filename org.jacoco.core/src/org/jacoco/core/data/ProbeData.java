/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jacoco.core.data;

public interface ProbeData {

	void merge(ProbeData other);

	void reset();

	int getLength();

	boolean isCovered(int probeId);

	int getHitCount();

	void setCovered(int probeId);

}
