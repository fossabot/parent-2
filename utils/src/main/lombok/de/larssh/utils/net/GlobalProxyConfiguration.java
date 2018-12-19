package de.larssh.utils.net;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;

import java.net.InetSocketAddress;
import java.net.Proxy.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import de.larssh.utils.text.Strings;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enumeration of network protocols that allow proxy related properties,
 * described at <a href=
 * "https://docs.oracle.com/javase/8/docs/api/java/net/doc-files/net-properties.html">Network
 * Properties</a>.
 *
 * <p>
 * <b>Usage example 1:</b> The following shows how to set a global HTTP proxy.
 *
 * <pre>
 * InetSocketAddress inetSocketAddress = new InetSocketAddress("proxy.example.com", "8080");
 * GlobalProxyConfiguration.HTTP.setGlobalProxy(inetSocketAddress);
 * </pre>
 *
 * <p>
 * <b>Usage example 2:</b> The following shows how to unset the global HTTP
 * proxy.
 *
 * <pre>
 * GlobalProxyConfiguration.HTTP.unsetGlobalProxy();
 * </pre>
 */
@Getter
@RequiredArgsConstructor
public enum GlobalProxyConfiguration {
	/**
	 * FTP proxy related settings
	 */
	FTP(Type.HTTP,
			80,
			"ftp.proxyHost",
			"ftp.proxyPort",
			Optional.of("ftp.nonProxyHosts"),
			Optional.of(Arrays.asList("localhost", "127.*", "[::1]"))),

	/**
	 * HTTP proxy related settings
	 */
	HTTP(Type.HTTP,
			80,
			"http.proxyHost",
			"http.proxyPort",
			Optional.of("http.nonProxyHosts"),
			Optional.of(Arrays.asList("localhost", "127.*", "[::1]"))),

	/**
	 * HTTPS proxy related settings
	 *
	 * <p>
	 * The HTTPS protocol handler will use the same {@code nonProxyHosts} property
	 * as the HTTP protocol. Use {@link #HTTP} to modify that property.
	 */
	HTTPS(Type.HTTP, 443, "https.proxyHost", "https.proxyPort", Optional.empty(), Optional.empty()),

	/**
	 * SOCKS proxy related properties
	 *
	 * <p>
	 * The SOCKS protocol handler does not support the {@ocde nonProxyHosts}
	 * property.
	 */
	SOCKS(Type.SOCKS, 1080, "socksProxyHost", "socksProxyPort", Optional.empty(), Optional.empty());

	/**
	 * Proxy type
	 */
	Type type;

	/**
	 * Default proxy port
	 */
	int defaultPort;

	/**
	 * Host property name
	 */
	String hostProperty;

	/**
	 * Port property name
	 */
	String portProperty;

	/**
	 * {@code nonProxyHosts} property name
	 */
	Optional<String> nonProxyHostsProperty;

	/**
	 * Default {@code nonProxyHosts}
	 */
	Optional<Collection<String>> defaultNonProxyHosts;

	/**
	 * Appends all of {@code nonProxyHosts} to the current protocols
	 * {@code nonProxyHosts} property if it is not already present.
	 *
	 * <p>
	 * For protocols not supporting the {@code nonProxyHosts} property an
	 * {@link UnsupportedOperationException} is thrown.
	 *
	 * @param nonProxyHosts any number of hosts that should be accessed
	 *                      <b>without</b> going through the proxy
	 * @throws UnsupportedOperationException for protocols not supporting the
	 *                                       {@code nonProxyHosts} property
	 */
	public void addNonProxyHosts(final String... nonProxyHosts) {
		final Set<String> set = getNonProxyHosts();
		set.addAll(Arrays.asList(nonProxyHosts));
		setNonProxyHosts(set);
	}

	/**
	 * Returns all patterns of the current protocols {@code nonProxyHosts} property.
	 *
	 * <p>
	 * For protocols not supporting the {@code nonProxyHosts} property an
	 * {@link UnsupportedOperationException} is thrown.
	 *
	 * @return patterns of the current protocols {@code nonProxyHosts} property
	 * @throws UnsupportedOperationException for protocols not supporting the
	 *                                       {@code nonProxyHosts} property
	 */
	public Set<String> getNonProxyHosts() {
		return new LinkedHashSet<>(Arrays.asList(
				Optional.ofNullable(System.getProperty(getNonProxyHostsPropertyOrThrow())).orElse("").split("\\|")));
	}

	/**
	 * Returns the current protocols {@code nonProxyHosts} property name.
	 *
	 * <p>
	 * For protocols not supporting the {@code nonProxyHosts} property an
	 * {@link UnsupportedOperationException} is thrown.
	 *
	 * @return the current protocols {@code nonProxyHosts} property name
	 * @throws UnsupportedOperationException for protocols not supporting the
	 *                                       {@code nonProxyHosts} property
	 */
	private String getNonProxyHostsPropertyOrThrow() {
		return getNonProxyHostsProperty().orElseThrow(() -> new UnsupportedOperationException(
				Strings.format("The protocol %s does not support the nonProxyHosts property.", name())));
	}

	/**
	 * Removes all given patterns from the current protocols {@code nonProxyHosts}
	 * property.
	 *
	 * <p>
	 * For protocols not supporting the {@code nonProxyHosts} property an
	 * {@link UnsupportedOperationException} is thrown.
	 *
	 * @param nonProxyHosts any number of hosts that should not be accessed
	 *                      <b>with</b> going through the proxy
	 * @throws UnsupportedOperationException for protocols not supporting the
	 *                                       {@code nonProxyHosts} property
	 */
	public void removeNonProxyHosts(final String... nonProxyHosts) {
		final Set<String> set = getNonProxyHosts();
		set.removeAll(Arrays.asList(nonProxyHosts));
		setNonProxyHosts(set);
	}

	/**
	 * Resets the current protocols {@code nonProxyHosts} property.
	 *
	 * <p>
	 * For protocols not supporting the {@code nonProxyHosts} property an
	 * {@link UnsupportedOperationException} is thrown.
	 *
	 * <p>
	 * Afterwards nothing but the default {@code nonProxyHosts} are set.
	 *
	 * @throws UnsupportedOperationException for protocols not supporting the
	 *                                       {@code nonProxyHosts} property
	 */
	public void resetNonProxyHosts() {
		setNonProxyHosts(getDefaultNonProxyHosts().orElse(emptyList()));
	}

	/**
	 * Overwrites the JVM global current protocols proxy properties with the given
	 * host and port.
	 *
	 * @param inetSocketAddress host and port to apply globally
	 */
	public void setGlobalProxy(final InetSocketAddress inetSocketAddress) {
		System.setProperty(getHostProperty(), inetSocketAddress.getHostString());
		System.setProperty(getPortProperty(), Integer.toString(inetSocketAddress.getPort()));
	}

	/**
	 * Overwrites the current protocols {@code nonProxyHosts} property with the
	 * given patterns.
	 *
	 * <p>
	 * For protocols not supporting the {@code nonProxyHosts} property an
	 * {@link UnsupportedOperationException} is thrown.
	 *
	 * @param nonProxyHosts any number of hosts that should be accessed
	 *                      <b>without</b> going through the proxy
	 * @throws UnsupportedOperationException for protocols not supporting the
	 *                                       {@code nonProxyHosts} property
	 */
	public void setNonProxyHosts(final Collection<String> nonProxyHosts) {
		System.setProperty(getNonProxyHostsPropertyOrThrow(), nonProxyHosts.stream().distinct().collect(joining("|")));
	}

	/**
	 * Overwrites the JVM global current protocols proxy properties.
	 */
	public void unsetGlobalProxy() {
		System.clearProperty(getHostProperty());
		System.clearProperty(getPortProperty());
	}
}