/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.agent.rt.controller;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import org.jacoco.core.runtime.IRemoteCommandVisitor;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;

/**
 * Handler for a single socket based remote connection.
 */
class TcpConnection implements IRemoteCommandVisitor {

	private final IRuntime runtime;

	private final Socket socket;

	private RemoteControlWriter writer;

	private RemoteControlReader reader;

	private boolean initialized;

	public TcpConnection(final Socket socket, final IRuntime runtime) {
		this.socket = socket;
		this.runtime = runtime;
		this.initialized = false;
	}

	public void init() throws IOException {
		this.writer = new RemoteControlWriter(socket.getOutputStream());
		this.reader = new RemoteControlReader(socket.getInputStream());
		this.reader.setRemoteCommandVisitor(this);
		this.initialized = true;
	}

	/**
	 * Processes all requests for this session until the socket is closed.
	 * 
	 * @throws IOException
	 *             in case of problems whith the connection
	 */
	public void run() throws IOException {
		try {
			while (reader.read()) {
			}
		} catch (final SocketException e) {
			// If the local socket is closed while polling for commands the
			// SocketException is expected.
			if (!socket.isClosed()) {
				throw e;
			}
		} finally {
			close();
		}
	}

	/**
	 * Dumps the current execution data if the connection is already initialized
	 * and the underlying socket is still open.
	 * 
	 * @throws IOException
	 */
	public void writeExecutionData() throws IOException {
		if (initialized && !socket.isClosed()) {
			visitDumpCommand(true, false);
		}
	}

	/**
	 * Closes the underlying socket if not closed yet.
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		if (!socket.isClosed()) {
			socket.close();
		}
	}

	// === IRemoteCommandVisitor ===

	public void visitDumpCommand(final boolean dump, final boolean reset)
			throws IOException {
		if (dump) {
			runtime.collect(writer, writer, reset);
		} else {
			if (reset) {
				runtime.reset();
			}
		}
		writer.sendCmdOk();
	}

}
