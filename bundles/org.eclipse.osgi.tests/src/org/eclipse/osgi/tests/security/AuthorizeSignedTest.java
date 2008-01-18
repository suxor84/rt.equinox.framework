/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.osgi.tests.security;

import junit.framework.Test;
import org.eclipse.core.tests.session.ConfigurationSessionTestSuite;
import org.eclipse.osgi.service.security.AuthorizationEvent;
import org.eclipse.osgi.service.security.AuthorizationListener;
import org.eclipse.osgi.tests.OSGiTestsActivator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

public class AuthorizeSignedTest extends BaseSecurityTest {

	protected void setUp() throws Exception {
		registerEclipseTrustEngine();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public static Test suite() {
		ConfigurationSessionTestSuite suite = new ConfigurationSessionTestSuite(BUNDLE_SECURITY_TESTS, "Unit tests for AuthorizationEngine with 'signed' policy");
		addDefaultSecurityBundles(suite);
		setAuthorizationEnabled(suite);
		setAuthorizationPolicy(suite, "signed");
		//for (int i = 0; i < s_tests.length; i++) {
		//	suite.addTest(s_tests[i]);
		suite.addTestSuite(AuthorizeSignedTest.class);
		//}
		return suite;
	}

	//test01: signed (allow)
	static boolean s_test01called = false;

	public void testAuthorize01() {

		Bundle testBundle = null;
		try {
			OSGiTestsActivator.getContext().registerService(AuthorizationListener.class.getName(), new AuthorizationListener() {
				public void authorizationEvent(AuthorizationEvent event) {
					assertEquals("Content was allowed!", AuthorizationEvent.ALLOWED, event.getResult());
					s_test01called = true;
				}
			}, null);

			testBundle = installBundle(getTestJarPath("signed")); //signed by ca1_leafa

			assertTrue("Handler not called!", s_test01called);

		} catch (Throwable t) {
			fail("unexpected exception", t);
		} finally {
			try {
				if (testBundle != null) {
					testBundle.uninstall();
				}
			} catch (BundleException e) {
				fail("Failed to uninstall bundle", e);
			}
		}
	}

	//test02: trusted (allow)
	static boolean s_test02called = false;

	public void testAuthorize02() {

		Bundle testBundle = null;
		try {
			getTrustEngine().addTrustAnchor(getTestCertificate("ca1_leafa"), "ca1_leafa");

			OSGiTestsActivator.getContext().registerService(AuthorizationListener.class.getName(), new AuthorizationListener() {
				public void authorizationEvent(AuthorizationEvent event) {
					assertEquals("Content was allowed!", AuthorizationEvent.ALLOWED, event.getResult());
					s_test02called = true;
				}
			}, null);

			testBundle = installBundle(getTestJarPath("signed")); //signed by ca1_leafa

			assertTrue("Handler not called!", s_test02called);

		} catch (Throwable t) {
			fail("unexpected exception", t);
		} finally {
			try {
				getTrustEngine().removeTrustAnchor("ca1_leafa");
				if (testBundle != null) {
					testBundle.uninstall();
				}
			} catch (Throwable t) {
				fail("Failed to uninstall bundle", t);
			}
		}
	}

	//test03: unsigned (deny)
	static boolean s_test03called = false;

	public void testAuthorize03() {

		Bundle testBundle = null;
		try {
			OSGiTestsActivator.getContext().registerService(AuthorizationListener.class.getName(), new AuthorizationListener() {
				public void authorizationEvent(AuthorizationEvent event) {
					assertEquals("Content was allowed!", AuthorizationEvent.DENIED, event.getResult());
					s_test03called = true;
				}
			}, null);

			testBundle = installBundle(getTestJarPath("unsigned")); //signed by ca1_leafa

			assertTrue("Handler not called!", s_test03called);

		} catch (Throwable t) {
			fail("unexpected exception", t);
		} finally {
			try {
				if (testBundle != null) {
					testBundle.uninstall();
				}
			} catch (Throwable t) {
				fail("Failed to uninstall bundle", t);
			}
		}
	}

	//test04: corrupted (allow, then explode later)
	static boolean s_test04called = false;

	public void testAuthorize04() {

		Bundle testBundle = null;
		try {
			getTrustEngine().addTrustAnchor(getTestCertificate("ca1_leafa"), "ca1_leafa");

			OSGiTestsActivator.getContext().registerService(AuthorizationListener.class.getName(), new AuthorizationListener() {
				public void authorizationEvent(AuthorizationEvent event) {
					assertEquals("Content was allowed!", AuthorizationEvent.ALLOWED, event.getResult());
					s_test04called = true;
				}
			}, null);

			testBundle = installBundle(getTestJarPath("signed_with_corrupt")); //signed by ca1_leafa

			assertTrue("Handler not called!", s_test04called);

		} catch (Throwable t) {
			fail("unexpected exception", t);
		} finally {
			try {
				getTrustEngine().removeTrustAnchor("ca1_leafa");
				if (testBundle != null) {
					testBundle.uninstall();
				}
			} catch (Throwable t) {
				fail("Failed to uninstall bundle", t);
			}
		}
	}
	//test05: expired (deny) //TODO!
}