package com.github.ronniedong.expectforjava;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Ignore;
import org.junit.Test;

import com.github.ronniedong.expectforjava.Expect.EOFException;
import com.github.ronniedong.expectforjava.Expect.TimeoutException;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.InteractiveCallback;
import ch.ethz.ssh2.Session;

public class DemoTest {
	@Ignore("for manual use only")
	@Test
	public void expectHello() throws IOException, TimeoutException, EOFException {

		final Connection connection = new Connection("gentoo-mac.transmode.se");
		connection.connect();
		try {
			final String username = "mac";
			if (connection.isAuthMethodAvailable(username, "publickey")) {
				System.out
						.println("--> public key auth method supported by server");
			} else {
				System.out
						.println("--> public key auth method not supported by server");
			}
			if (connection.isAuthMethodAvailable(username,
					"keyboard-interactive")) {
				System.out
						.println("--> keyboard interactive auth method supported by server");
			} else {
				System.out
						.println("--> keyboard interactive auth method not supported by server");
			}
			if (connection.isAuthMethodAvailable(username, "password")) {
				System.out
						.println("--> password auth method supported by server");
			} else {
				System.out
						.println("--> password auth method not supported by server");
			}
			connection.authenticateWithKeyboardInteractive("user",
					new InteractiveCallback() {

						@Override
						public String[] replyToChallenge(String name,
								String instruction, int numPrompts,
								String[] prompt, boolean[] echo) {
							if (numPrompts == 0) {
								return new String[] {};
							} else {
								return new String[] { "password" };
							}
						}
					});
			final Session session = connection.openSession();
			session.requestDumbPTY();
			session.startShell();
			try {
				final Expect expect = new Expect(session.getStdout(),
						session.getStdin());
				expect.setDefault_timeout(1);
				try {
					expect.expectOrThrow("$");
					expect.send("echo hello\n");
					expect.expectOrThrow("hello");
					expect.expectOrThrow("$");
				} finally {
					expect.close();
				}
				System.out.println(expect.before + expect.match);
			} finally {
				session.close();
			}
		} finally {
			connection.close();
		}
	}
}
