/*
 Copyright (C) 2005-2007 MySQL AB

 This program is free software; you can redistribute it and/or modify
 it under the terms of version 2 of the GNU General Public License as 
 published by the Free Software Foundation.

 There are special exceptions to the terms and conditions of the GPL 
 as it is applied to this software. View the full text of the 
 exception in file EXCEPTIONS-CONNECTOR-J in the directory of this 
 software distribution.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.mysql.jdbc.log;

import com.mysql.jdbc.Util;
import com.mysql.jdbc.profiler.ProfilerEvent;

public class LogUtils {

    public static final String CALLER_INFORMATION_NOT_AVAILABLE = "Caller information not available";

    private static final String LINE_SEPARATOR = System
			.getProperty("line.separator");

	private static final int LINE_SEPARATOR_LENGTH = LINE_SEPARATOR.length();

	public static Object expandProfilerEventIfNecessary(
			Object possibleProfilerEvent) {

		if (possibleProfilerEvent instanceof ProfilerEvent) {
			StringBuffer msgBuf = new StringBuffer();

			ProfilerEvent evt = (ProfilerEvent) possibleProfilerEvent;

			Throwable locationException = evt.getEventCreationPoint();

			if (locationException == null) {
				locationException = new Throwable();
			}

			msgBuf.append("Profiler Event: [");

			boolean appendLocationInfo = false;
			
			switch (evt.getEventType()) {
			case ProfilerEvent.TYPE_EXECUTE:
				msgBuf.append("EXECUTE");

				break;

			case ProfilerEvent.TYPE_FETCH:
				msgBuf.append("FETCH");

				break;

			case ProfilerEvent.TYPE_OBJECT_CREATION:
				msgBuf.append("CONSTRUCT");

				break;

			case ProfilerEvent.TYPE_PREPARE:
				msgBuf.append("PREPARE");

				break;

			case ProfilerEvent.TYPE_QUERY:
				msgBuf.append("QUERY");

				break;

			case ProfilerEvent.TYPE_WARN:
				msgBuf.append("WARN");
				appendLocationInfo = true;
				
				break;
				
			case ProfilerEvent.TYPE_SLOW_QUERY:
				msgBuf.append("SLOW QUERY");
				appendLocationInfo = false;
				
				break;
				
			default:
				msgBuf.append("UNKNOWN");
			}

			msgBuf.append("] ");
			msgBuf.append(findCallingClassAndMethod(locationException));
			msgBuf.append(" duration: ");
			msgBuf.append(evt.getEventDuration());
			msgBuf.append(" ");
			msgBuf.append(evt.getDurationUnits());
			msgBuf.append(", connection-id: ");
			msgBuf.append(evt.getConnectionId());
			msgBuf.append(", statement-id: ");
			msgBuf.append(evt.getStatementId());
			msgBuf.append(", resultset-id: ");
			msgBuf.append(evt.getResultSetId());

			String evtMessage = evt.getMessage();

			if (evtMessage != null) {
				msgBuf.append(", message: ");
				msgBuf.append(evtMessage);
			}

			if (appendLocationInfo) {
				msgBuf
					.append("\n\nFull stack trace of location where event occurred:\n\n");
				msgBuf.append(Util.stackTraceToString(locationException));
				msgBuf.append("\n");
			}
			
			return msgBuf;
		}
		
		return possibleProfilerEvent;
	}

	public static String findCallingClassAndMethod(Throwable t) {
		String stackTraceAsString = Util.stackTraceToString(t);

		String callingClassAndMethod = CALLER_INFORMATION_NOT_AVAILABLE;

		int endInternalMethods = stackTraceAsString
				.lastIndexOf("com.mysql.jdbc");

		if (endInternalMethods != -1) {
			int endOfLine = -1;
			int compliancePackage = stackTraceAsString.indexOf(
					"com.mysql.jdbc.compliance", endInternalMethods);

			if (compliancePackage != -1) {
				endOfLine = compliancePackage - LINE_SEPARATOR_LENGTH;
			} else {
				endOfLine = stackTraceAsString.indexOf(LINE_SEPARATOR,
						endInternalMethods);
			}

			if (endOfLine != -1) {
				int nextEndOfLine = stackTraceAsString.indexOf(LINE_SEPARATOR,
						endOfLine + LINE_SEPARATOR_LENGTH);

				if (nextEndOfLine != -1) {
					callingClassAndMethod = stackTraceAsString.substring(
							endOfLine + LINE_SEPARATOR_LENGTH, nextEndOfLine);
				} else {
					callingClassAndMethod = stackTraceAsString
							.substring(endOfLine + LINE_SEPARATOR_LENGTH);
				}
			}
		}

		if (!callingClassAndMethod.startsWith("\tat ") && 
				!callingClassAndMethod.startsWith("at ")) {
			return "at " + callingClassAndMethod;
		}

		return callingClassAndMethod;
	}
}
