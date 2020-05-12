// Copyright (C) 2001 - 2011 Philip Aston
// All rights reserved.
//
// This file is part of The Grinder software distribution. Refer to
// the file LICENSE which is part of The Grinder distribution for
// licensing details. The Grinder distribution is available on the
// Internet at http://grinder.sourceforge.net/
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
// FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
// COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
// INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
// STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
// OF THE POSSIBILITY OF SUCH DAMAGE.

package net.grinder.console.model;

import net.grinder.common.GrinderProperties;
import net.grinder.communication.CommunicationDefaults;
import net.grinder.console.common.ConsoleException;
import net.grinder.console.common.DisplayMessageConsoleException;
import net.grinder.console.common.Resources;
import net.grinder.util.Directory;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


/**
 * Class encapsulating the console options.
 * (Add extra properties field for additional control of the console in nGrinder)
 *
 * @author Philip Aston
 *
 */
public final class ConsoleProperties {

	/** Property name. */
	public static final String COLLECT_SAMPLES_PROPERTY =
		"grinder.console.numberToCollect";

	/** Property name. */
	public static final String IGNORE_SAMPLES_PROPERTY =
		"grinder.console.numberToIgnore";

	/** Property name. */
	public static final String SAMPLE_INTERVAL_PROPERTY =
		"grinder.console.sampleInterval";

	/** Property name. */
	public static final String SIG_FIG_PROPERTY =
		"grinder.console.significantFigures";

	/** Property name. */
	public static final String CONSOLE_HOST_PROPERTY =
		"grinder.console.consoleHost";

	/** Property name. */
	public static final String CONSOLE_PORT_PROPERTY =
		"grinder.console.consolePort";

	/** Property name. */
	public static final String RESET_CONSOLE_WITH_PROCESSES_PROPERTY =
		"grinder.console.resetConsoleWithProcesses";

	/** Property name. */
	public static final String RESET_CONSOLE_WITH_PROCESSES_ASK_PROPERTY =
		"grinder.console.resetConsoleWithProcessesAsk";

	/** Property name. */
	public static final String PROPERTIES_NOT_SET_ASK_PROPERTY =
		"grinder.console.propertiesNotSetAsk";

	/** Property name. */
	public static final String START_WITH_UNSAVED_BUFFERS_ASK_PROPERTY =
		"grinder.console.startWithUnsavedBuffersAsk";

	/** Property name. */
	public static final String STOP_PROCESSES_ASK_PROPERTY =
		"grinder.console.stopProcessesAsk";

	/** Property name. */
	public static final String DISTRIBUTE_ON_START_ASK_PROPERTY =
		"grinder.console.distributeAutomaticallyAsk";

	/** Property name. */
	public static final String PROPERTIES_FILE_PROPERTY =
		"grinder.console.propertiesFile";

	/** Property name. */
	public static final String DISTRIBUTION_DIRECTORY_PROPERTY =
		"grinder.console.scriptDistributionDirectory";

	/** Property name. */
	public static final String DISTRIBUTION_FILE_FILTER_EXPRESSION_PROPERTY =
		"grinder.console.distributionFileFilterExpression";

	/**
	 * Default regular expression for filtering distribution files.
	 */
	public static final String DEFAULT_DISTRIBUTION_FILE_FILTER_EXPRESSION =
		"^CVS/$|" +
			"^\\.svn/$|" +
			"^.*~$|" +
			"^(out_|error_|data_)\\w+-\\d+\\.log\\d*$";

	/** Property name. */
	public static final String SCAN_DISTRIBUTION_FILES_PERIOD_PROPERTY =
		"grinder.console.scanDistributionFilesPeriod";

	/** Property name. */
	public static final String LOOK_AND_FEEL_PROPERTY =
		"grinder.console.lookAndFeel";

	/** Property name. */
	public static final String EXTERNAL_EDITOR_COMMAND_PROPERTY =
		"grinder.console.externalEditorCommand";

	/** Property name. */
	public static final String EXTERNAL_EDITOR_ARGUMENTS_PROPERTY =
		"grinder.console.externalEditorArguments";

	/** Property name. */
	public static final String FRAME_BOUNDS_PROPERTY =
		"grinder.console.frameBounds";

	/** Property name. */
	public static final String SAVE_TOTALS_WITH_RESULTS_PROPERTY =
		"grinder.console.saveTotalsWithResults";

	private final PropertyChangeSupport m_changeSupport =
		new PropertyChangeSupport(this);

	private final List<Property> m_propertyList = new ArrayList<Property>();

	private final IntProperty m_collectSampleCount =
		new IntProperty(COLLECT_SAMPLES_PROPERTY, 0);

	private final IntProperty m_ignoreSampleCount =
		new IntProperty(IGNORE_SAMPLES_PROPERTY, 0);

	private final IntProperty m_sampleInterval =
		new IntProperty(SAMPLE_INTERVAL_PROPERTY, 1000);

	private final IntProperty m_significantFigures =
		new IntProperty(SIG_FIG_PROPERTY, 3);

	private final BooleanProperty m_resetConsoleWithProcesses =
		new BooleanProperty(RESET_CONSOLE_WITH_PROCESSES_PROPERTY, false);

	private final FileProperty m_propertiesFile =
		new FileProperty(PROPERTIES_FILE_PROPERTY);

	private final DirectoryProperty m_distributionDirectory =
		new DirectoryProperty(DISTRIBUTION_DIRECTORY_PROPERTY);

	private final PatternProperty m_distributionFileFilterPattern =
		new PatternProperty(
			DISTRIBUTION_FILE_FILTER_EXPRESSION_PROPERTY,
			DEFAULT_DISTRIBUTION_FILE_FILTER_EXPRESSION);

	private final IntProperty m_scanDistributionFilesPeriod =
		new IntProperty(SCAN_DISTRIBUTION_FILES_PERIOD_PROPERTY, 6000);

	private final StringProperty m_lookAndFeel =
		new StringProperty(LOOK_AND_FEEL_PROPERTY, null);

	private final FileProperty m_externalEditorCommand =
		new FileProperty(EXTERNAL_EDITOR_COMMAND_PROPERTY);

	private final StringProperty m_externalEditorArguments =
		new StringProperty(EXTERNAL_EDITOR_ARGUMENTS_PROPERTY, null);

	private final RectangleProperty m_frameBounds =
		new RectangleProperty(FRAME_BOUNDS_PROPERTY);

	private final BooleanProperty m_resetConsoleWithProcessesAsk =
		new BooleanProperty(RESET_CONSOLE_WITH_PROCESSES_ASK_PROPERTY, true);

	private final BooleanProperty m_propertiesNotSetAsk =
		new BooleanProperty(PROPERTIES_NOT_SET_ASK_PROPERTY, true);

	private final BooleanProperty m_startWithUnsavedBuffersAsk =
		new BooleanProperty(START_WITH_UNSAVED_BUFFERS_ASK_PROPERTY, true);

	private final BooleanProperty m_stopProcessesAsk =
		new BooleanProperty(STOP_PROCESSES_ASK_PROPERTY, true);

	private final BooleanProperty m_distributeOnStartAsk =
		new BooleanProperty(DISTRIBUTE_ON_START_ASK_PROPERTY, true);

	private final StringProperty m_consoleHost =
		new StringProperty(CONSOLE_HOST_PROPERTY,
			CommunicationDefaults.CONSOLE_HOST);

	private final IntProperty m_consolePort =
		new IntProperty(CONSOLE_PORT_PROPERTY, CommunicationDefaults.CONSOLE_PORT);

	private final BooleanProperty m_saveTotalsWithResults =
		new BooleanProperty(SAVE_TOTALS_WITH_RESULTS_PROPERTY, false);

	private final Resources m_resources;

	/**
	 * Use to save and load properties, and to keep track of the
	 * associated file.
	 */
	private final GrinderProperties m_properties;

	private final Map<String, Object> m_extraProperties = new HashMap<>();

	/**
	 * Construct a ConsoleProperties backed by the given file.
	 *
	 * @param resources Console resources.
	 * @param file The properties file.
	 * @throws ConsoleException If the properties file
	 * cannot be read or the properties file contains invalid data.
	 *
	 */
	public ConsoleProperties(Resources resources, File file)
		throws ConsoleException {

		m_resources = resources;

		try {
			m_properties = new GrinderProperties(file);
		}
		catch (GrinderProperties.PersistenceException e) {
			throw new DisplayMessageConsoleException(
				m_resources, "couldNotLoadOptionsError.text", e);
		}

		for (Property property : m_propertyList) {
			property.setFromProperties();
		}
	}

	/**
	 * Copy constructor. Does not copy property change listeners.
	 *
	 * @param properties The properties to copy.
	 */
	public ConsoleProperties(ConsoleProperties properties) {
		m_resources = properties.m_resources;
		m_properties = properties.m_properties;
		set(properties);
	}

	/**
	 * Assignment. Does not copy property change listeners, nor the
	 * associated file.
	 *
	 * @param properties The properties to copy.
	 */
	public void set(ConsoleProperties properties) {
		m_collectSampleCount.set(properties.getCollectSampleCount());
		m_ignoreSampleCount.set(properties.getIgnoreSampleCount());
		m_sampleInterval.set(properties.getSampleInterval());
		m_significantFigures.set(properties.getSignificantFigures());
		m_consoleHost.set(properties.getConsoleHost());
		m_consolePort.set(properties.getConsolePort());
		m_resetConsoleWithProcesses.set(properties.getResetConsoleWithProcesses());
		m_propertiesFile.set(properties.getPropertiesFile());
		m_distributionDirectory.set(properties.getDistributionDirectory());
		m_distributionFileFilterPattern.set(
			properties.getDistributionFileFilterPattern());
		m_scanDistributionFilesPeriod.set(
			properties.getScanDistributionFilesPeriod());
		m_lookAndFeel.set(properties.getLookAndFeel());
		m_externalEditorCommand.set(properties.getExternalEditorCommand());
		m_externalEditorArguments.set(properties.getExternalEditorArguments());
		m_frameBounds.set(properties.getFrameBounds());
		m_resetConsoleWithProcessesAsk.set(
			properties.getResetConsoleWithProcessesAsk());
		m_propertiesNotSetAsk.set(properties.getPropertiesNotSetAsk());
		m_startWithUnsavedBuffersAsk.set(
			properties.getStartWithUnsavedBuffersAsk());
		m_stopProcessesAsk.set(properties.getStopProcessesAsk());
		m_distributeOnStartAsk.set(properties.getDistributeOnStartAsk());
		m_saveTotalsWithResults.set(properties.getSaveTotalsWithResults());
	}

	/**
	 * Add a <code>PropertyChangeListener</code>.
	 *
	 * @param listener The listener.
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		m_changeSupport.addPropertyChangeListener(listener);
	}

	/**
	 * Add a <code>PropertyChangeListener</code> which listens to a
	 * particular property.
	 *
	 * @param property The property.
	 * @param listener The listener.
	 */
	public void addPropertyChangeListener(
		String property, PropertyChangeListener listener) {
		m_changeSupport.addPropertyChangeListener(property, listener);
	}

	/**
	 * Save to the associated file.
	 *
	 * @throws ConsoleException If an error occurs.
	 */
	public void save() throws ConsoleException {
		for (Property property : m_propertyList) {
			property.setToProperties();
		}

		try {
			m_properties.save();
		}
		catch (GrinderProperties.PersistenceException e) {
			throw new DisplayMessageConsoleException(
				m_resources, "couldNotSaveOptionsError.text", e);
		}
	}

	/**
	 * Get the number of samples to collect.
	 *
	 * @return The number.
	 */
	public int getCollectSampleCount() {
		return m_collectSampleCount.get();
	}

	/**
	 * Set the number of samples to collect.
	 *
	 * @param n The number. 0 => forever.
	 * @throws ConsoleException If the number is negative.
	 */
	public void setCollectSampleCount(int n) throws ConsoleException {
		if (n < 0) {
			throw new DisplayMessageConsoleException(
				m_resources, "collectNegativeError.text");
		}

		m_collectSampleCount.set(n);
	}

	/**
	 * Get the number of samples to ignore.
	 *
	 * @return The number.
	 */
	public int getIgnoreSampleCount() {
		return m_ignoreSampleCount.get();
	}

	/**
	 * Set the number of samples to collect.
	 *
	 * @param n The number. Must be positive.
	 * @throws ConsoleException If the number is negative or zero.
	 */
	public void setIgnoreSampleCount(int n) throws ConsoleException {
		if (n < 0) {
			throw new DisplayMessageConsoleException(
				m_resources, "ignoreSamplesNegativeError.text");
		}

		m_ignoreSampleCount.set(n);
	}

	/**
	 * Get the sample interval.
	 *
	 * @return The interval in milliseconds.
	 */
	public int getSampleInterval() {
		return m_sampleInterval.get();
	}

	/**
	 * Set the sample interval.
	 *
	 * @param interval The interval in milliseconds.
	 * @throws ConsoleException If the number is negative or zero.
	 */
	public void setSampleInterval(int interval) throws ConsoleException {
		if (interval <= 0) {
			throw new DisplayMessageConsoleException(
				m_resources, "intervalLessThanOneError.text");
		}

		m_sampleInterval.set(interval);
	}

	/**
	 * Get the number of significant figures.
	 *
	 * @return The number of significant figures.
	 */
	public int getSignificantFigures() {
		return m_significantFigures.get();
	}

	/**
	 * Set the number of significant figures.
	 *
	 * @param n The number of significant figures.
	 * @throws ConsoleException If the number is negative.
	 */
	public void setSignificantFigures(int n) throws ConsoleException {
		if (n <= 0) {
			throw new DisplayMessageConsoleException(
				m_resources, "significantFiguresNegativeError.text");
		}

		m_significantFigures.set(n);
	}

	/**
	 * Get the console host as a string.
	 *
	 * @return The address.
	 */
	public String getConsoleHost() {
		return m_consoleHost.get();
	}

	/**
	 * Set the console host.
	 *
	 * @param s Either a machine name or the IP address.
	 * @throws ConsoleException If the address is not
	 * valid.
	 */
	public void setConsoleHost(String s) throws ConsoleException {
		// We treat any address that we can look up as valid. I guess we
		// could also try binding to it to discover whether it is local,
		// but that could take an indeterminate amount of time.

		if (s.length() > 0) {    // Empty string => all local hosts.
			final InetAddress newAddress;

			try {
				newAddress = InetAddress.getByName(s);
			}
			catch (UnknownHostException e) {
				throw new DisplayMessageConsoleException(
					m_resources, "unknownHostError.text");
			}

			if (newAddress.isMulticastAddress()) {
				throw new DisplayMessageConsoleException(
					m_resources, "invalidConsoleHostError.text");
			}
		}

		m_consoleHost.set(s);
	}

	/**
	 * Get the console port.
	 *
	 * @return The port.
	 */
	public int getConsolePort() {
		return m_consolePort.get();
	}

	/**
	 * Set the console port.
	 *
	 * @param i The port number.
	 * @throws ConsoleException If the port number is not sensible.
	 */
	public void setConsolePort(int i) throws ConsoleException {
		if (i < CommunicationDefaults.MIN_PORT ||
			i > CommunicationDefaults.MAX_PORT) {
			throw new DisplayMessageConsoleException(
				m_resources,
				"invalidPortNumberError.text",
				new Object[] {
					CommunicationDefaults.MIN_PORT,
					CommunicationDefaults.MAX_PORT, }
			);
		}

		m_consolePort.set(i);
	}

	/**
	 * Get whether the console should be reset with the worker
	 * processes.
	 *
	 * @return <code>true</code> => the console should be reset with the
	 * worker processes.
	 */
	public boolean getResetConsoleWithProcesses() {
		return m_resetConsoleWithProcesses.get();
	}

	/**
	 * Set whether the console should be reset with the worker
	 * processes.
	 *
	 * @param b <code>true</code> => the console should be reset with
	 * the worker processes.
	 */
	public void setResetConsoleWithProcesses(boolean b) {
		m_resetConsoleWithProcesses.set(b);
	}

	/**
	 * Get whether the user wants to be asked if console should be reset
	 * with the worker processes.
	 *
	 * @return <code>true</code> => the user wants to be asked.
	 */
	public boolean getResetConsoleWithProcessesAsk() {
		return m_resetConsoleWithProcessesAsk.get();
	}

	/**
	 * Set and save whether the user wants to be asked if console should be reset
	 * with the worker processes.
	 *
	 * @param value
	 *          <code>true</code> => the user wants to be asked.
	 * @throws ConsoleException
	 *            If the property couldn't be persisted
	 */
	public void setResetConsoleWithProcessesAsk(boolean value)
		throws ConsoleException {
		m_resetConsoleWithProcessesAsk.set(value);
		m_resetConsoleWithProcessesAsk.save();
	}

	/**
	 * Get whether the user wants to be asked if console should be reset
	 * with the worker processes.
	 *
	 * @return <code>true</code> => the user wants to be asked.
	 */
	public boolean getPropertiesNotSetAsk() {
		return m_propertiesNotSetAsk.get();
	}

	/**
	 * Set and save whether the user wants to be asked if console should be reset
	 * with the worker processes.
	 *
	 * @param value
	 *          <code>true</code> => the user wants to be asked.
	 * @throws ConsoleException
	 *           If the property couldn't be persisted.
	 */
	public void setPropertiesNotSetAsk(boolean value) throws ConsoleException {
		m_propertiesNotSetAsk.set(value);
		m_propertiesNotSetAsk.save();
	}


	/**
	 * Get whether the user wants to be warned when starting processes
	 * with unsaved buffers.
	 *
	 * @return <code>true</code> => the user wants to be warned.
	 */
	public boolean getStartWithUnsavedBuffersAsk() {
		return m_startWithUnsavedBuffersAsk.get();
	}

	/**
	 * Set and save whether the user wants to be warned when starting processes
	 * with unsaved buffers.
	 *
	 * @param value
	 *          <code>true</code> => the user wants to be warned.
	 * @throws ConsoleException
	 *           If the property couldn't be persisted.
	 */
	public void setStartWithUnsavedBuffersAsk(boolean value)
		throws ConsoleException {
		m_startWithUnsavedBuffersAsk.set(value);
		m_startWithUnsavedBuffersAsk.save();
	}

	/**
	 * Get whether the user wants to be asked to confirm that processes
	 * should be stopped.
	 *
	 * @return <code>true</code> => the user wants to be asked.
	 */
	public boolean getStopProcessesAsk() {
		return m_stopProcessesAsk.get();
	}

	/**
	 * Set and save whether the user wants to be asked to confirm that processes
	 * should be stopped.
	 *
	 * @param value
	 *          <code>true</code> => the user wants to be asked.
	 * @throws ConsoleException If the property couldn't be persisted.
	 */
	public void setStopProcessesAsk(boolean value)
		throws ConsoleException {
		m_stopProcessesAsk.set(value);
		m_stopProcessesAsk.save();
	}

	/**
	 * Get whether the user wants to distribute files automatically when starting
	 * processes.
	 *
	 * @return <code>true</code> => the user wants automatic distribution.
	 */
	public boolean getDistributeOnStartAsk() {
		return m_distributeOnStartAsk.get();
	}

	/**
	 * Set and save whether the user wants to distribute files automatically when
	 * starting processes.
	 *
	 * @param value
	 *          <code>true</code> => the user wants automatic distribution.
	 * @throws ConsoleException If the property couldn't be persisted.
	 */
	public void setDistributeOnStartAsk(boolean value)
		throws ConsoleException {
		m_distributeOnStartAsk.set(value);
		m_distributeOnStartAsk.save();
	}

	/**
	 * Get the properties file.
	 *
	 * @return The properties file. <code>null</code> => No file set.
	 */
	public File getPropertiesFile() {
		return m_propertiesFile.get();
	}

	/**
	 * Set and save the properties file.
	 *
	 * @param propertiesFile The properties file. <code>null</code> => No file
	 * set.
	 * @throws ConsoleException
	 * @throws ConsoleException If the property could not be saved.
	 */
	public void setAndSavePropertiesFile(File propertiesFile)
		throws ConsoleException {
		m_propertiesFile.set(propertiesFile);
		m_propertiesFile.save();
	}

	/**
	 * Get the script distribution directory.
	 *
	 * @return The directory.
	 */
	public Directory getDistributionDirectory() {
		return m_distributionDirectory.get();
	}

	/**
	 * Set and save the script distribution directory.
	 *
	 * @param distributionDirectory The directory.
	 * @throws ConsoleException If the property could not be saved.
	 */
	public void setAndSaveDistributionDirectory(Directory distributionDirectory)
		throws ConsoleException {
		m_distributionDirectory.set(distributionDirectory);
		m_distributionDirectory.save();
	}

	/**
	 * Get the distribution file filter pattern.
	 *
	 * <p>The original regular expression can be obtained with
	 * <code>getDistributionFileFilterPattern().getPattern</code>.</p>
	 *
	 * @return The pattern.
	 * @see #setDistributionFileFilterExpression
	 */
	public Pattern getDistributionFileFilterPattern() {
		return m_distributionFileFilterPattern.get();
	}

	/**
	 * Set the distribution file filter regular expression.
	 *
	 * <p>Files and directory names (not full paths) that match the
	 * regular expression are not distributed. Directory names are
	 * distinguished by a trailing '/'. The expression is in Perl 5
	 * format.</p>
	 *
	 * @param expression A Perl 5 format expression. <code>null</code>
	 * => use default pattern.
	 * @throws ConsoleException If the pattern is not valid.
	 */
	public void setDistributionFileFilterExpression(String expression)
		throws ConsoleException {
		m_distributionFileFilterPattern.set(expression);
	}

	/**
	 * Get the period at which the distribution files should be scanned.
	 *
	 * @return The period, in milliseconds.
	 */
	public int getScanDistributionFilesPeriod() {
		return m_scanDistributionFilesPeriod.get();
	}

	/**
	 * Set the console port.
	 *
	 * @param i The port number.
	 * @throws ConsoleException If the port number is not sensible.
	 */
	public void setScanDistributionFilesPeriod(int i) throws ConsoleException {
		if (i < 0) {
			throw new DisplayMessageConsoleException(
				m_resources, "scanDistributionFilesPeriodNegativeError.text");
		}

		m_scanDistributionFilesPeriod.set(i);
	}

	/**
	 * Get the name of the Look and Feel. It is up to the UI
	 * implementation how this is interpreted.
	 *
	 * @return The Look and Feel name. <code>null</code> => use default.
	 */
	public String getLookAndFeel() {
		return m_lookAndFeel.get();
	}

	/**
	 * Set the name of the Look and Feel.
	 *
	 * @param lookAndFeel The Look and Feel name. <code>null</code> =>
	 * use default.
	 */
	public void setLookAndFeel(String lookAndFeel) {
		m_lookAndFeel.set(lookAndFeel);
	}

	/**
	 * Get the external editor command.
	 *
	 * @return The path to the process to be used for external editing.
	 * <code>null</code> => no external editor set.
	 */
	public File getExternalEditorCommand() {
		return m_externalEditorCommand.get();
	}

	/**
	 * Set the external editor command.
	 *
	 * @param command The path to the process to be used for external editing.
	 * <code>null</code> => no external editor set.
	 */
	public void setExternalEditorCommand(File command) {
		m_externalEditorCommand.set(command);
	}

	/**
	 * Get the external editor arguments.
	 *
	 * @return The arguments to be used with the external editor.
	 */
	public String getExternalEditorArguments() {
		return m_externalEditorArguments.get();
	}

	/**
	 * Set the external editor arguments.
	 *
	 * @param arguments The arguments to be used with the external editor.
	 */
	public void setExternalEditorArguments(String arguments) {
		m_externalEditorArguments.set(arguments);
	}

	/**
	 * Get the location and size of the console frame.
	 *
	 * @return The console frame bounds.
	 */
	public Rectangle getFrameBounds() {
		return m_frameBounds.get();
	}

	/**
	 * Set and save the location and size of the console frame.
	 *
	 * @param bounds The console frame bounds.
	 * @throws ConsoleException If the property couldn't be persisted.
	 */
	public void setAndSaveFrameBounds(Rectangle bounds) throws ConsoleException {
		m_frameBounds.set(bounds);
		m_frameBounds.save();
	}

	/**
	 * Get whether saved results files should include the Totals line.
	 *
	 * @return {@code true} => results files should include totals.
	 */
	public boolean getSaveTotalsWithResults() {
		return m_saveTotalsWithResults.get();
	}

	/**
	 * Set whether saved results files should include the Totals line.
	 *
	 * @param b {@code true} => results files should include totals.
	 * @throws ConsoleException If the property couldn't be persisted.
	 */
	public void setSaveTotalsWithResults(boolean b) throws ConsoleException {
		m_saveTotalsWithResults.set(b);
		m_saveTotalsWithResults.save();
	}

	public Object getExtraProperties(String key) {
		return m_extraProperties.get(key);
	}

	public void putExtraProperties(String key, Object value) {
		m_extraProperties.put(key, value);
	}

	private abstract class Property {
		private final String m_propertyName;
		private final Object m_defaultValue;
		private Object m_value;

		Property(String propertyName, Object defaultValue) {
			m_propertyName = propertyName;
			m_defaultValue = defaultValue;
			m_value = defaultValue;
			m_propertyList.add(this);
		}

		abstract void setFromProperties() throws ConsoleException;

		abstract void setToProperties();

		public final void save() throws ConsoleException {
			setToProperties();

			try {
				m_properties.saveSingleProperty(m_propertyName);
			}
			catch (GrinderProperties.PersistenceException e) {
				throw new DisplayMessageConsoleException(
					m_resources, "couldNotSaveOptionsError.text", e);
			}
		}

		protected final String getPropertyName() {
			return m_propertyName;
		}

		protected final Object getDefaultValue() {
			return m_defaultValue;
		}

		protected final Object getValue() {
			return m_value;
		}

		protected final void setValue(Object value) {
			final Object old = m_value;
			m_value = value;
			m_changeSupport.firePropertyChange(getPropertyName(), old, m_value);
		}
	}

	private final class StringProperty extends Property {
		public StringProperty(String propertyName, String defaultValue) {
			super(propertyName, defaultValue);
		}

		public void setFromProperties() {
			set(m_properties.getProperty(getPropertyName(),
				(String) getDefaultValue()));
		}

		public void setToProperties() {
			if (get() != getDefaultValue()) {
				m_properties.setProperty(getPropertyName(), get());
			}
			else {
				m_properties.remove(getPropertyName());
			}
		}

		public String get() {
			return (String) getValue();
		}

		public void set(String s) {
			setValue(s);
		}
	}

	private final class PatternProperty extends Property {
		public PatternProperty(String propertyName, String defaultExpression) {
			super(propertyName, Pattern.compile(defaultExpression));
		}

		public void setFromProperties() throws ConsoleException {
			set(m_properties.getProperty(getPropertyName(), null));
		}

		public void setToProperties() {
			if (get() != getDefaultValue()) {
				m_properties.setProperty(getPropertyName(), get().pattern());
			}
			else {
				m_properties.remove(getPropertyName());
			}
		}

		public Pattern get() {
			return (Pattern) getValue();
		}

		public void set(String expression) throws ConsoleException {
			if (expression == null) {
				set((Pattern) getDefaultValue());
			}
			else {
				try {
					set(Pattern.compile(expression));
				}
				catch (PatternSyntaxException e) {
					throw new DisplayMessageConsoleException(
						m_resources,
						"regularExpressionError.text",
						new Object[] { getPropertyName(), },
						e);
				}
			}
		}

		public void set(Pattern pattern) {
			setValue(pattern);
		}
	}

	private final class IntProperty extends Property {
		public IntProperty(String propertyName, int defaultValue) {
			super(propertyName, defaultValue);
		}

		public void setFromProperties() {
			set(m_properties.getInt(getPropertyName(),
				((Integer)getDefaultValue()).intValue()));
		}

		public void setToProperties() {
			m_properties.setInt(getPropertyName(), get());
		}

		public int get() {
			return ((Integer)getValue()).intValue();
		}

		public void set(int i) {
			setValue(i);
		}
	}

	private final class FileProperty extends Property {
		public FileProperty(String propertyName) {
			super(propertyName, null);
		}

		public void setFromProperties() {
			set(m_properties.getFile(getPropertyName(), null));
		}

		public void setToProperties() {
			if (get() != getDefaultValue()) {
				m_properties.setFile(getPropertyName(), get());
			}
			else {
				m_properties.remove(getPropertyName());
			}
		}

		public File get() {
			return (File) getValue();
		}

		public void set(File file) {
			setValue(file);
		}
	}

	private final class DirectoryProperty extends Property {
		public DirectoryProperty(String propertyName) {
			super(propertyName, new Directory());
		}

		public void setFromProperties() {
			set(m_properties.getFile(getPropertyName(), null));
		}

		public void setToProperties() {
			m_properties.setFile(getPropertyName(), get().getFile());
		}

		public Directory get() {
			return (Directory) getValue();
		}

		public void set(File file) {
			if (file == null) {
				set(new Directory());
			}
			else {
				try {
					set(new Directory(file));
				}
				catch (Directory.DirectoryException e) {
					set(new Directory());
				}
			}
		}

		public void set(Directory file) {
			setValue(file);
		}
	}

	private final class BooleanProperty extends Property {
		public BooleanProperty(String propertyName, boolean defaultValue) {
			super(propertyName, defaultValue);
		}

		public void setFromProperties() {
			set(m_properties.getBoolean(getPropertyName(),
				((Boolean)getDefaultValue()).booleanValue()));
		}

		public void setToProperties() {
			m_properties.setBoolean(getPropertyName(), get());
		}

		public boolean get() {
			return ((Boolean)getValue()).booleanValue();
		}

		public void set(boolean b) {
			setValue(b);
		}
	}

	private final class RectangleProperty extends Property {
		public RectangleProperty(String propertyName) {
			super(propertyName, null);
		}

		public void setFromProperties() {
			final String property =
				m_properties.getProperty(getPropertyName(), null);

			if (property == null) {
				set(null);
			}
			else {
				final StringTokenizer tokenizer = new StringTokenizer(property, ",");

				try {
					set(new Rectangle(
						Integer.parseInt(tokenizer.nextToken()),
						Integer.parseInt(tokenizer.nextToken()),
						Integer.parseInt(tokenizer.nextToken()),
						Integer.parseInt(tokenizer.nextToken())));
				}
				catch (NoSuchElementException e) {
					set(null);
				}
				catch (NumberFormatException e) {
					set(null);
				}
			}
		}

		public void setToProperties() {
			final Rectangle value = get();

			if (value != getDefaultValue()) {
				m_properties.setProperty(
					getPropertyName(),
					value.x + "," + value.y + "," + value.width + "," + value.height);
			}
			else {
				m_properties.remove(getPropertyName());
			}
		}

		public Rectangle get() {
			return (Rectangle) getValue();
		}

		public void set(Rectangle rectangle) {
			setValue(rectangle);
		}
	}
}
