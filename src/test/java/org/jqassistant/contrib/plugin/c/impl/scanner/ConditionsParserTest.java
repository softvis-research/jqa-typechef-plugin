package org.jqassistant.contrib.plugin.c.impl.scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.hamcrest.CoreMatchers;
import org.jqassistant.contrib.plugin.c.api.ConditionsLexer;
import org.jqassistant.contrib.plugin.c.api.ConditionsParser;
import org.jqassistant.contrib.plugin.c.api.model.AndDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.ConditionDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.NegationDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.OrDescriptor;
import org.jqassistant.contrib.plugin.c.api.model.SingleConditionDescriptor;
import org.junit.Test;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;

public class ConditionsParserTest extends AbstractPluginIT{

	@Test
	public void testSingleCondition() {
		String text = "definedEx(FLAG)";
		
		store.beginTransaction();
		ConditionsLexer conditionsLexer = new ConditionsLexer(CharStreams.fromString(text));
		CommonTokenStream tokens = new CommonTokenStream(conditionsLexer);
		ConditionsParser parser = new ConditionsParser(tokens);
		ParseTree tree = parser.completeCondition();
		ParseTreeWalker walker = new ParseTreeWalker();
		CConditionsListener listener = new CConditionsListener(this.store);
		walker.walk(listener, tree);
		ConditionDescriptor descriptor = listener.getResultCondition();
		assertThat(descriptor, CoreMatchers.<Descriptor>instanceOf(SingleConditionDescriptor.class));
		SingleConditionDescriptor singleConditionDescriptor = (SingleConditionDescriptor) descriptor;
		assertEquals("FLAG", singleConditionDescriptor.getMacroName());
		
		store.commitTransaction();
	}
	
	@Test
	public void testNegation() {
		String text = "!definedEx(FLAG)";
		
		store.beginTransaction();
		ConditionsLexer conditionsLexer = new ConditionsLexer(CharStreams.fromString(text));
		CommonTokenStream tokens = new CommonTokenStream(conditionsLexer);
		ConditionsParser parser = new ConditionsParser(tokens);
		ParseTree tree = parser.completeCondition();
		ParseTreeWalker walker = new ParseTreeWalker();
		CConditionsListener listener = new CConditionsListener(this.store);
		walker.walk(listener, tree);
		ConditionDescriptor descriptor = listener.getResultCondition();
		assertThat(descriptor, CoreMatchers.<Descriptor>instanceOf(NegationDescriptor.class));
		NegationDescriptor negationDescriptor = (NegationDescriptor) descriptor;
		SingleConditionDescriptor singleCondition = (SingleConditionDescriptor) negationDescriptor.getCondition();
		assertEquals("FLAG", singleCondition.getMacroName());
		
		store.commitTransaction();
	}
	
	@Test
	public void testAnd() {
		String text = "(definedEx(FLAG2) &amp;&amp; !definedEx(FLAG))";
		
		store.beginTransaction();
		ConditionsLexer conditionsLexer = new ConditionsLexer(CharStreams.fromString(text));
		CommonTokenStream tokens = new CommonTokenStream(conditionsLexer);
		ConditionsParser parser = new ConditionsParser(tokens);
		ParseTree tree = parser.completeCondition();
		ParseTreeWalker walker = new ParseTreeWalker();
		CConditionsListener listener = new CConditionsListener(this.store);
		walker.walk(listener, tree);
		ConditionDescriptor descriptor = listener.getResultCondition();
		assertThat(descriptor, CoreMatchers.<Descriptor>instanceOf(AndDescriptor.class));
		AndDescriptor andDescriptor = (AndDescriptor) descriptor;
		assertEquals(2, andDescriptor.getConnectedConditions().size());
		for(ConditionDescriptor condition : andDescriptor.getConnectedConditions()) {
			if(condition instanceof SingleConditionDescriptor) {
				assertEquals("FLAG2", ((SingleConditionDescriptor) condition).getMacroName());
			} else if(condition instanceof NegationDescriptor) {
				NegationDescriptor negation = (NegationDescriptor) condition;
				assertEquals("FLAG", ((SingleConditionDescriptor)negation.getCondition()).getMacroName());
			}
		}
		
		store.commitTransaction();
	}
	
	@Test
	public void testComplexAnd() {
		String text = "(definedEx(__CURL_CURL_H) &amp;&amp; definedEx(HEADER_CURL_LLIST_H) &amp;&amp; !definedEx(HEADER_CURL_FILEINFO_H))";
		
		store.beginTransaction();
		ConditionsLexer conditionsLexer = new ConditionsLexer(CharStreams.fromString(text));
		CommonTokenStream tokens = new CommonTokenStream(conditionsLexer);
		ConditionsParser parser = new ConditionsParser(tokens);
		ParseTree tree = parser.completeCondition();
		ParseTreeWalker walker = new ParseTreeWalker();
		CConditionsListener listener = new CConditionsListener(this.store);
		walker.walk(listener, tree);
		ConditionDescriptor descriptor = listener.getResultCondition();
		assertThat(descriptor, CoreMatchers.<Descriptor>instanceOf(AndDescriptor.class));
		AndDescriptor andDescriptor = (AndDescriptor) descriptor;
		assertEquals(3, andDescriptor.getConnectedConditions().size());
		for(ConditionDescriptor condition : andDescriptor.getConnectedConditions()) {
			if(condition instanceof SingleConditionDescriptor) {
				SingleConditionDescriptor singleCondition = (SingleConditionDescriptor) condition;
				List<String> nameList = new ArrayList<>(Arrays.asList("__CURL_CURL_H", "HEADER_CURL_LLIST_H"));
				assertTrue(nameList.contains(singleCondition.getMacroName()));
			} else if(condition instanceof NegationDescriptor) {
				NegationDescriptor negation = (NegationDescriptor) condition;
				assertEquals("HEADER_CURL_FILEINFO_H", ((SingleConditionDescriptor)negation.getCondition()).getMacroName());
			}
		}
		
		store.commitTransaction();
	}
	
	@Test
	public void testOr() {
		String text = "(definedEx(HEADER_CURL_FILEINFO_H) || !definedEx(__VMS))";
		
		store.beginTransaction();
		ConditionsLexer conditionsLexer = new ConditionsLexer(CharStreams.fromString(text));
		CommonTokenStream tokens = new CommonTokenStream(conditionsLexer);
		ConditionsParser parser = new ConditionsParser(tokens);
		ParseTree tree = parser.completeCondition();
		ParseTreeWalker walker = new ParseTreeWalker();
		CConditionsListener listener = new CConditionsListener(this.store);
		walker.walk(listener, tree);
		ConditionDescriptor descriptor = listener.getResultCondition();
		assertThat(descriptor, CoreMatchers.<Descriptor>instanceOf(OrDescriptor.class));
		OrDescriptor orDescriptor = (OrDescriptor) descriptor;
		assertEquals(2, orDescriptor.getConnectedConditions().size());
		for(ConditionDescriptor condition : orDescriptor.getConnectedConditions()) {
			if(condition instanceof SingleConditionDescriptor) {
				SingleConditionDescriptor singleCondition = (SingleConditionDescriptor) condition;
				assertEquals("HEADER_CURL_FILEINFO_H", singleCondition.getMacroName());
			} else if(condition instanceof NegationDescriptor) {
				NegationDescriptor negation = (NegationDescriptor) condition;
				assertEquals("__VMS", ((SingleConditionDescriptor)negation.getCondition()).getMacroName());
			}
		}
		
		store.commitTransaction();
	}
	
	@Test
	public void testComplexOr() {
		String text = "(definedEx(HEADER_CURL_FILEINFO_H) || !definedEx(__VMS) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H) || definedEx(HEADER_CURL_SETUP_VMS_H) || !definedEx(__VAX))";
		
		store.beginTransaction();
		ConditionsLexer conditionsLexer = new ConditionsLexer(CharStreams.fromString(text));
		CommonTokenStream tokens = new CommonTokenStream(conditionsLexer);
		ConditionsParser parser = new ConditionsParser(tokens);
		ParseTree tree = parser.completeCondition();
		ParseTreeWalker walker = new ParseTreeWalker();
		CConditionsListener listener = new CConditionsListener(this.store);
		walker.walk(listener, tree);
		ConditionDescriptor descriptor = listener.getResultCondition();
		assertThat(descriptor, CoreMatchers.<Descriptor>instanceOf(OrDescriptor.class));
		OrDescriptor orDescriptor = (OrDescriptor) descriptor;
		assertEquals(6, orDescriptor.getConnectedConditions().size());
		int singleConditionCounter = 0;
		int negativeConditionCounter = 0;
		for(ConditionDescriptor condition : orDescriptor.getConnectedConditions()) {
			if(condition instanceof SingleConditionDescriptor) {
				singleConditionCounter += 1;
			} else if(condition instanceof NegationDescriptor) {
				negativeConditionCounter += 1;
			}
		}
		
		assertEquals(4, singleConditionCounter);
		assertEquals(2, negativeConditionCounter);
		
		store.commitTransaction();
	}
	
	@Test
	public void testComplexCondition() {
		String text = "((definedEx(HEADER_CURL_FILEINFO_H) || !definedEx(__VMS) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H) || definedEx(HEADER_CURL_SETUP_VMS_H) || !definedEx(__VAX)) &amp;&amp; (definedEx(HEADER_CURL_FILEINFO_H) || !definedEx(__VMS) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H) || definedEx(HEADER_CURL_SETUP_VMS_H)) &amp;&amp; (definedEx(HEADER_CURL_LLIST_H) || definedEx(HEADER_CURL_FILEINFO_H) || definedEx(HEADER_CURL_SETUP_H) || (!definedEx(HAVE_EXTRA_STRICMP_H) &amp;&amp; (definedEx(HEADER_CURL_FILEINFO_H) || !definedEx(macintosh) || definedEx(HAVE_CONFIG_H) || definedEx(HEADER_CURL_CONFIG_MAC_H) || !definedEx(__MRC__) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H)))) &amp;&amp; (definedEx(HEADER_CURL_FILEINFO_H) || definedEx(_WIN32_WCE) || definedEx(WIN32) || definedEx(__CURL_CURL_H) || (!definedEx(__SYMBIAN32__) &amp;&amp; (definedEx(_WIN32) || definedEx(__WIN32__)) &amp;&amp; !definedEx(HEADER_CURL_FILEINFO_H) &amp;&amp; !definedEx(__CURL_CURL_H))) &amp;&amp; (definedEx(HEADER_CURL_LLIST_H) || definedEx(HEADER_CURL_FILEINFO_H)) &amp;&amp; (definedEx(HEADER_CURL_FILEINFO_H) || defined(WIN32) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H) || !definedEx(MSDOS)) &amp;&amp; (definedEx(HEADER_CURL_FILEINFO_H) || definedEx(__LWIP_OPT_H__) || definedEx(_WIN32_WCE) || definedEx(_WINSOCK_H) || definedEx(__CURL_CURL_H) || definedEx(__CYGWIN__) || definedEx(_WINSOCKAPI_) || definedEx(LWIP_HDR_OPT_H) || (!definedEx(WIN32) &amp;&amp; (definedEx(__SYMBIAN32__) || (!definedEx(_WIN32) &amp;&amp; !definedEx(__WIN32__)) || definedEx(HEADER_CURL_FILEINFO_H) || definedEx(__CURL_CURL_H)))) &amp;&amp; (definedEx(HEADER_CURL_LLIST_H) || definedEx(HEADER_CURL_FILEINFO_H) || definedEx(HEADER_CURL_SETUP_H) || !definedEx(__VXWORKS__)) &amp;&amp; (!definedEx(__AMIGA__) || definedEx(HEADER_CURL_FILEINFO_H) || definedEx(__ixemul__) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H)) &amp;&amp; (definedEx(HEADER_CURL_FILEINFO_H) || !defined(HAVE_WINDOWS_H) || !defined(HAVE_WINSOCK_H) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H) || defined(HAVE_WINSOCK2_H)) &amp;&amp; (definedEx(HEADER_CURL_LLIST_H) || definedEx(HEADER_CURL_FILEINFO_H) || definedEx(HEADER_CURL_SETUP_H) || !definedEx(WINAPI_FAMILY)) &amp;&amp; (definedEx(HEADER_CURL_FILEINFO_H) || !defined(HAVE_SYS_TIME_H) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H) || definedEx(HEADER_CURL_SETUP_ONCE_H)) &amp;&amp; (definedEx(HEADER_CURL_FILEINFO_H) || (!definedEx(HAVE_BOOL_T) &amp;&amp; (definedEx(HEADER_CURL_CONFIG_VXWORKS_H) || definedEx(HEADER_CURL_FILEINFO_H) || !definedEx(__VXWORKS__) || definedEx(HAVE_CONFIG_H) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H))) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H) || !defined(HAVE_STDBOOL_H) || definedEx(HEADER_CURL_SETUP_ONCE_H)) &amp;&amp; (definedEx(HEADER_CURL_LLIST_H) || definedEx(HEADER_CURL_FILEINFO_H) || definedEx(HEADER_CURL_SETUP_H) || !definedEx(__POCC__)) &amp;&amp; (definedEx(HEADER_CURL_FILEINFO_H) || !defined(HAVE_SYS_SOCKET_H) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H) || definedEx(HEADER_CURL_SETUP_ONCE_H)) &amp;&amp; (definedEx(__CURL_CURL_H) || definedEx(HEADER_CURL_FILEINFO_H) || !definedEx(__BEOS__)) &amp;&amp; (definedEx(__CURL_CURL_H) || definedEx(HEADER_CURL_FILEINFO_H) || (!definedEx(__SYMBIAN32__) &amp;&amp; !definedEx(__FreeBSD_version) &amp;&amp; !definedEx(__minix) &amp;&amp; !definedEx(__NOVELL_LIBC__) &amp;&amp; !definedEx(__CYGWIN__) &amp;&amp; !definedEx(__NetBSD__) &amp;&amp; !definedEx(__OpenBSD__) &amp;&amp; !definedEx(__INTEGRITY) &amp;&amp; !definedEx(_AIX) &amp;&amp; !definedEx(__ANDROID__) &amp;&amp; !definedEx(ANDROID))) &amp;&amp; (!definedEx(__POCC__) || definedEx(HEADER_CURL_FILEINFO_H) || definedEx(HEADER_CURL_CONFIG_WIN32_H) || definedEx(_WIN32_WCE) || !defined(WIN32) || definedEx(HAVE_CONFIG_H) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H) || definedEx(_MSC_VER)) &amp;&amp; (definedEx(HEADER_CURL_FILEINFO_H) || !defined(WIN32) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H) || definedEx(HEADER_CURL_SETUP_ONCE_H)) &amp;&amp; (definedEx(__CURL_CURL_H) || definedEx(HEADER_CURL_FILEINFO_H) || definedEx(__CURL_SYSTEM_H) || !defined(CURL_PULL_SYS_TYPES_H)) &amp;&amp; (!definedEx(USE_ARES) || definedEx(HEADER_CURL_FILEINFO_H) || definedEx(HEADER_CURL_CONFIG_WIN32_H) || definedEx(_WIN32_WCE) || !defined(WIN32) || definedEx(HAVE_CONFIG_H) || (!definedEx(USE_THREADS_WIN32) &amp;&amp; (definedEx(HEADER_CURL_FILEINFO_H) || definedEx(HEADER_CURL_CONFIG_WIN32_H) || definedEx(_WIN32_WCE) || definedEx(USE_SYNC_DNS) || !defined(WIN32) || definedEx(HAVE_CONFIG_H) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H) || definedEx(USE_ARES))) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H)) &amp;&amp; (definedEx(HEADER_CURL_FILEINFO_H) || !defined(HAVE_TIME_H) || defined(HAVE_SYS_TIME_H) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H) || definedEx(HEADER_CURL_SETUP_ONCE_H)) &amp;&amp; (definedEx(HEADER_CURL_FILEINFO_H) || !defined(TIME_WITH_SYS_TIME) || !defined(HAVE_SYS_TIME_H) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H) || definedEx(HEADER_CURL_SETUP_ONCE_H)) &amp;&amp; !definedEx(HEADER_CURL_FILEINFO_H) &amp;&amp; (definedEx(HEADER_CURL_FILEINFO_H) || !defined(HAVE_WINSOCK2_H) || !defined(HAVE_WS2TCPIP_H) || !defined(HAVE_WINDOWS_H) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H)) &amp;&amp; (defined(STDC_HEADERS) || definedEx(HEADER_CURL_FILEINFO_H) || definedEx(__STDC_HEADERS_H) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H)) &amp;&amp; (definedEx(HEADER_CURL_LLIST_H) || definedEx(HEADER_CURL_FILEINFO_H) || definedEx(HEADER_CURL_SETUP_H) || !definedEx(CURL_WANTS_CA_BUNDLE_ENV)) &amp;&amp; (definedEx(__CURL_CURL_H) || definedEx(HEADER_CURL_FILEINFO_H) || definedEx(__CURL_SYSTEM_H) || !defined(CURL_PULL_WS2TCPIP_H)) &amp;&amp; (definedEx(HEADER_CURL_LLIST_H) || definedEx(HEADER_CURL_FILEINFO_H) || definedEx(HEADER_CURL_SETUP_H) || (!definedEx(HAVE_EXTRA_STRDUP_H) &amp;&amp; (definedEx(HEADER_CURL_FILEINFO_H) || !definedEx(macintosh) || definedEx(HAVE_CONFIG_H) || definedEx(HEADER_CURL_CONFIG_MAC_H) || !definedEx(__MRC__) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H)))) &amp;&amp; (definedEx(HEADER_CURL_FILEINFO_H) || !defined(HAVE_SYS_TYPES_H) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H) || definedEx(HEADER_CURL_SETUP_ONCE_H)) &amp;&amp; (definedEx(HEADER_CURL_FILEINFO_H) || (!definedEx(HEADER_CURL_SETUP_H) &amp;&amp; !definedEx(HEADER_CURL_CONFIG_VXWORKS_H) &amp;&amp; !definedEx(HAVE_CONFIG_H) &amp;&amp; !definedEx(HEADER_CURL_FILEINFO_H) &amp;&amp; !definedEx(HEADER_CURL_LLIST_H) &amp;&amp; definedEx(__VXWORKS__)) || (!definedEx(USE_DARWINSSL) &amp;&amp; !definedEx(USE_WINDOWS_SSPI) &amp;&amp; !definedEx(USE_MBEDTLS) &amp;&amp; !definedEx(USE_GNUTLS) &amp;&amp; !definedEx(USE_OS400CRYPTO) &amp;&amp; !defined(USE_OPENSSL) &amp;&amp; (definedEx(HEADER_CURL_FILEINFO_H) || !definedEx(__OS400__) || definedEx(HAVE_CONFIG_H) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H) || definedEx(HEADER_CURL_CONFIG_OS400_H)) &amp;&amp; !definedEx(USE_NSS) &amp;&amp; (definedEx(HEADER_CURL_FILEINFO_H) || definedEx(HEADER_CURL_CONFIG_WIN32_H) || definedEx(_WIN32_WCE) || !defined(WIN32) || definedEx(HAVE_CONFIG_H) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H)) &amp;&amp; !definedEx(USE_WIN32_CRYPTO)) || !definedEx(USE_MBEDTLS) || definedEx(CURL_DISABLE_NTLM) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H) || definedEx(CURL_DISABLE_CRYPTO_AUTH)) &amp;&amp; (definedEx(HEADER_CURL_LLIST_H) || definedEx(HEADER_CURL_FILEINFO_H) || definedEx(HEADER_CURL_SETUP_H) || !definedEx(__SALFORDC__)) &amp;&amp; (definedEx(__CURL_CURL_H) || definedEx(HEADER_CURL_FILEINFO_H) || definedEx(__CURL_SYSTEM_H) || !defined(CURL_PULL_SYS_SOCKET_H)) &amp;&amp; (definedEx(HEADER_CURL_FILEINFO_H) || (!definedEx(CURL_DOES_CONVERSIONS) &amp;&amp; (definedEx(HEADER_CURL_FILEINFO_H) || definedEx(HEADER_CURL_CONFIG_TPF_H) || !definedEx(TPF) || definedEx(HAVE_CONFIG_H) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H))) || definedEx(HEADER_CURL_CTYPE_H) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H) || definedEx(HEADER_CURL_SETUP_ONCE_H)) &amp;&amp; (definedEx(HEADER_CURL_FILEINFO_H) || definedEx(ALLOW_MSVC6_WITHOUT_PSDK) || (defined(HAVE_WINDOWS_H) &amp;&amp; definedEx(_FILETIME_)) || !definedEx(_MSC_VER) || definedEx(__POCC__) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H)) &amp;&amp; (definedEx(__CURL_CURL_H) || definedEx(HEADER_CURL_FILEINFO_H) || definedEx(__CURL_SYSTEM_H) || (!definedEx(CURL_PULL_SYS_POLL_H) &amp;&amp; (definedEx(__CURL_CURL_H) || definedEx(HEADER_CURL_FILEINFO_H) || definedEx(__CURL_SYSTEM_H) || !definedEx(_AIX)))) &amp;&amp; (definedEx(HEADER_CURL_LLIST_H) || definedEx(HEADER_CURL_FILEINFO_H) || definedEx(HEADER_CURL_SETUP_H) || !defined(USE_WIN32_SMALL_FILES)) &amp;&amp; (definedEx(__WATCOMC__) || definedEx(HEADER_CURL_FILEINFO_H) || definedEx(WIN32) || definedEx(__VXWORKS__) || definedEx(__CURL_CURL_H) || (!definedEx(__SYMBIAN32__) &amp;&amp; (definedEx(_WIN32) || definedEx(__WIN32__)) &amp;&amp; !definedEx(HEADER_CURL_FILEINFO_H) &amp;&amp; !definedEx(__CURL_CURL_H))) &amp;&amp; (definedEx(HEADER_CURL_FILEINFO_H) || !definedEx(NEED_MEMORY_H) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H) || definedEx(HEADER_CURL_SETUP_ONCE_H)) &amp;&amp; (definedEx(HEADER_CURL_FILEINFO_H) || !defined(HAVE_WINSOCK2_H) || !defined(HAVE_WINDOWS_H) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H)) &amp;&amp; (definedEx(HEADER_CURL_FILEINFO_H) || !defined(NEED_MALLOC_H) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H) || definedEx(HEADER_CURL_SETUP_ONCE_H)) &amp;&amp; (definedEx(HEADER_CURL_FILEINFO_H) || (!definedEx(SOCKET) &amp;&amp; !defined(HAVE_WINSOCK2_H) &amp;&amp; !defined(HAVE_WS2TCPIP_H) &amp;&amp; !defined(USE_WINSOCK) &amp;&amp; !defined(HAVE_WINSOCK_H) &amp;&amp; (definedEx(HEADER_CURL_FILEINFO_H) || definedEx(HEADER_CURL_CONFIG_WIN32_H) || definedEx(_WIN32_WCE) || !defined(WIN32) || definedEx(HAVE_CONFIG_H) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H) || !definedEx(USE_WATT32))) || (!definedEx(__LWIP_OPT_H__) &amp;&amp; !definedEx(LWIP_HDR_OPT_H)) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H)) &amp;&amp; (definedEx(HEADER_CURL_LLIST_H) || definedEx(HEADER_CURL_FILEINFO_H) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_SETUP_ONCE_H)) &amp;&amp; (definedEx(HEADER_CURL_FILEINFO_H) || !definedEx(USE_WIN32_IDN) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H) || (!definedEx(USE_LIBIDN2) &amp;&amp; (definedEx(HEADER_CURL_FILEINFO_H) || definedEx(USE_WIN32_IDN) || !definedEx(HAVE_LIBIDN2) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H) || !definedEx(HAVE_IDN2_H)))) &amp;&amp; (!definedEx(__POCC__) || definedEx(HEADER_CURL_FILEINFO_H) || definedEx(HEADER_CURL_CONFIG_WIN32_H) || definedEx(_WIN32_WCE) || definedEx(__POCC__OLDNAMES) || !defined(WIN32) || definedEx(HAVE_CONFIG_H) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H)) &amp;&amp; (definedEx(HEADER_CURL_LLIST_H) || definedEx(HEADER_CURL_FILEINFO_H) || definedEx(HEADER_CURL_SETUP_H) || !definedEx(TPF)) &amp;&amp; (definedEx(HEADER_CURL_FILEINFO_H) || definedEx(HEADER_CURL_CONFIG_WIN32_H) || definedEx(_WIN32_WCE) || !defined(WIN32) || definedEx(HAVE_CONFIG_H) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H) || !definedEx(USE_WATT32)) &amp;&amp; (definedEx(HEADER_CURL_FILEINFO_H) || !defined(HAVE_NETDB_H) || !definedEx(__VMS) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H) || definedEx(HEADER_CURL_SETUP_VMS_H)) &amp;&amp; (definedEx(HEADER_CURL_FILEINFO_H) || definedEx(__NOVELL_LIBC__) || !definedEx(NETWARE) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H)) &amp;&amp; (!definedEx(HAVE_CONFIG_H) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H) || definedEx(HEADER_CURL_FILEINFO_H)) &amp;&amp; (definedEx(HEADER_CURL_FILEINFO_H) || !defined(HAVE_ERRNO_H) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H) || definedEx(HEADER_CURL_SETUP_ONCE_H)) &amp;&amp; (definedEx(__CURL_CURL_H) || definedEx(HEADER_CURL_FILEINFO_H)) &amp;&amp; (definedEx(HEADER_CURL_LLIST_H) || definedEx(HEADER_CURL_FILEINFO_H) || definedEx(HEADER_CURL_SETUP_H) || !defined(USE_WIN32_LARGE_FILES)) &amp;&amp; (definedEx(HEADER_CURL_LLIST_H) || definedEx(HEADER_CURL_FILEINFO_H) || definedEx(HEADER_CURL_SETUP_H) || !defined(HAVE_ASSERT_H)) &amp;&amp; (definedEx(HEADER_CURL_FILEINFO_H) || !definedEx(__OS400__) || definedEx(HEADER_CURL_SETUP_OS400_H) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H)) &amp;&amp; (definedEx(HEADER_CURL_LLIST_H) || definedEx(HEADER_CURL_FILEINFO_H) || definedEx(HEADER_CURL_SETUP_H) || !defined(HAVE_WINDOWS_H)) &amp;&amp; (definedEx(HEADER_CURL_LLIST_H) || definedEx(HEADER_CURL_FILEINFO_H) || definedEx(HEADER_CURL_SETUP_H) || !definedEx(USE_LWIPSOCK)) &amp;&amp; (definedEx(HEADER_CURL_FILEINFO_H) || !defined(HAVE_SYS_STAT_H) || definedEx(HEADER_CURL_SETUP_H) || definedEx(HEADER_CURL_LLIST_H) || definedEx(HEADER_CURL_SETUP_ONCE_H)) &amp;&amp; (definedEx(HEADER_CURL_LLIST_H) || definedEx(HEADER_CURL_FILEINFO_H) || definedEx(HEADER_CURL_SETUP_H) || !definedEx(__TANDEM)))";
		
		store.beginTransaction();
		ConditionsLexer conditionsLexer = new ConditionsLexer(CharStreams.fromString(text));
		CommonTokenStream tokens = new CommonTokenStream(conditionsLexer);
		ConditionsParser parser = new ConditionsParser(tokens);
		ParseTree tree = parser.completeCondition();
		ParseTreeWalker walker = new ParseTreeWalker();
		CConditionsListener listener = new CConditionsListener(this.store);
		walker.walk(listener, tree);
		ConditionDescriptor descriptor = listener.getResultCondition();
		assertThat(descriptor, CoreMatchers.<Descriptor>instanceOf(AndDescriptor.class));
		AndDescriptor andDescriptor = (AndDescriptor) descriptor;
		assertEquals(59, andDescriptor.getConnectedConditions().size());

		store.commitTransaction();
	}
}
