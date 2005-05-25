/*
 * 1.3 03/12/19
 *
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package de.moonflower.jfritz;

import java.io.NotSerializableException;
import java.io.IOException;

/**
 * Thrown to indicate that an operation could not complete because the input did
 * not conform to the appropriate XML document type for a collection of
 * properties, as per the Properties specification.
 * <p>
 *
 * Note, that although InvalidPropertiesFormatException inherits Serializable
 * interface from Exception, it is not intended to be Serializable. Appropriate
 * serialization methods are implemented to throw NotSerializableException.
 *
 * @version 1.3 03/12/19
 * @since 1.5
 * @serial exclude
 */

public class InvalidPropertiesFormatException extends IOException {
	/**
	 * Constructs an InvalidPropertiesFormatException with the specified cause.
	 *
	 * @param cause
	 *            the cause
	 */
	public InvalidPropertiesFormatException(Throwable cause) {
		super(cause == null ? null : cause.toString());
		this.initCause(cause);
	}

	/**
	 * Constructs an InvalidPropertiesFormatException with the specified detail
	 * message.
	 *
	 * @param message
	 *            the detail message.
	 */
	public InvalidPropertiesFormatException(String message) {
		super(message);
	}

	/**
	 * Throws NotSerializableException, since InvalidPropertiesFormatException
	 * objects are not intended to be serializable.
	 */
	private void writeObject(java.io.ObjectOutputStream out)
			throws NotSerializableException {
		throw new NotSerializableException("Not serializable.");
	}

	/**
	 * Throws NotSerializableException, since InvalidPropertiesFormatException
	 * objects are not intended to be serializable.
	 */
	private void readObject(java.io.ObjectInputStream in)
			throws NotSerializableException {
		throw new NotSerializableException("Not serializable.");
	}

}
