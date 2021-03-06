
package org.ampproject;


import com.google.common.collect.ImmutableSet;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerPass;
import com.google.javascript.jscomp.Es6CompilerTestCase;


/**
 * Tests {@link AmpPass}.
 */
public class AmpPassTest extends Es6CompilerTestCase {

  ImmutableSet<String> suffixTypes = ImmutableSet.of(
      "dev.fine");

  @Override protected CompilerPass getProcessor(Compiler compiler) {
    return new AmpPass(compiler, /* isProd */ true, suffixTypes);
  }

  @Override protected int getNumRepetitions() {
    // This pass only runs once.
    return 1;
  }

  public void testDevFineRemoval() throws Exception {
    test(
        LINE_JOINER.join(
             "(function() {",
             "  var log = { dev: { fine: function() {} } };",
             "  log.dev.fine('hello world');",
             "  console.log('this is preserved');",
            "})()"),
        LINE_JOINER.join(
             "(function() {",
             "  var log = { dev: { fine: function() {} } };",
             "  console.log('this is preserved');",
            "})()"));
    test(
        LINE_JOINER.join(
             "(function() {",
             "  var log = { dev: { fine: function() {} } };",
             "  log.dev.fine();",
             "  console.log('this is preserved');",
            "})()"),
        LINE_JOINER.join(
             "(function() {",
             "  var log = { dev: { fine: function() {} } };",
             "  console.log('this is preserved');",
            "})()"));
  }

  public void testDevErrorPreserve() throws Exception {
    test(
        LINE_JOINER.join(
             "(function() {",
             "  var log = { dev: { error: function() {} } };",
             "  log.dev.error('hello world');",
             "  console.log('this is preserved');",
            "})()"),
        LINE_JOINER.join(
             "(function() {",
             "  var log = { dev: { error: function() {} } };",
             "  log.dev.error('hello world');",
             "  console.log('this is preserved');",
            "})()"));
  }

  public void testDevAssertExpressionRemoval() throws Exception {
    test(
        LINE_JOINER.join(
             "(function() {",
             "  var log = { dev: { assert: function() {} } };",
             "  log.dev.assert('hello world');",
             "  console.log('this is preserved');",
            "})()"),
        LINE_JOINER.join(
             "(function() {",
             "  var log = { dev: { assert: function() {} } };",
             "\"hello world\";",
             "  console.log('this is preserved');",
            "})()"));
    test(
        LINE_JOINER.join(
             "(function() {",
             "  var log = { dev: { assert: function() {} } };",
             "  var someValue = log.dev.assert();",
             "  console.log('this is preserved', someValue);",
            "})()"),
        LINE_JOINER.join(
             "(function() {",
             "  var log = { dev: { assert: function() {} } };",
             "  var someValue;",
             "  console.log('this is preserved', someValue);",
            "})()"));
  }

  public void testDevAssertPreserveFirstArg() throws Exception {
    test(
        LINE_JOINER.join(
             "(function() {",
             "  var log = { dev: { assert: function() {} } };",
             "  var someValue = log.dev.assert(true, 'This is an error');",
             "  console.log('this is preserved', someValue);",
            "})()"),
        LINE_JOINER.join(
             "(function() {",
             "  var log = { dev: { assert: function() {} } };",
             "  var someValue = true;",
             "  console.log('this is preserved', someValue);",
            "})()"));
  }

  public void testShouldPreserveNoneCalls() throws Exception {
    test(
        // Does reliasing
        LINE_JOINER.join(
             "(function() {",
             "  var log = { dev: { assert: function() {} } };",
             "  var someValue = log.dev.assert;",
             "  console.log('this is preserved', someValue);",
            "})()"),
        LINE_JOINER.join(
             "(function() {",
             "  var log = { dev: { assert: function() {} } };",
             "  var someValue = log.dev.assert;",
             "  console.log('this is preserved', someValue);",
            "})()"));
  }

  public void testGetModeLocalDevPropertyReplacement() throws Exception {
    test(
        LINE_JOINER.join(
             "(function() {",
             "function getMode() { return { localDev: true } }",
             "var $mode = { getMode: getMode };",
             "  if ($mode.getMode().localDev) {",
             "    console.log('hello world');",
             "  }",
            "})()"),
        LINE_JOINER.join(
             "(function() {",
             "function getMode() { return { localDev: true }; }",
             "var $mode = { getMode: getMode };",
             "  if (false) {",
             "    console.log('hello world');",
             "  }",
            "})()"));
  }

  public void testGetModeTestPropertyReplacement() throws Exception {
    test(
        LINE_JOINER.join(
             "(function() {",
             "function getMode() { return { test: true } }",
             "var $mode = { getMode: getMode };",
             "  if ($mode.getMode().test) {",
             "    console.log('hello world');",
             "  }",
            "})()"),
        LINE_JOINER.join(
             "(function() {",
             "function getMode() { return { test: true }; }",
             "var $mode = { getMode: getMode };",
             "  if (false) {",
             "    console.log('hello world');",
             "  }",
            "})()"));
  }

  public void testGetModeMinifiedPropertyReplacement() throws Exception {
    test(
        LINE_JOINER.join(
             "(function() {",
             "function getMode() { return { minified: false } }",
             "var $mode = { getMode: getMode };",
             "  if ($mode.getMode().minified) {",
             "    console.log('hello world');",
             "  }",
            "})()"),
        LINE_JOINER.join(
             "(function() {",
             "function getMode() { return { minified: false }; }",
             "var $mode = { getMode: getMode };",
             "  if (true) {",
             "    console.log('hello world');",
             "  }",
            "})()"));
  }

  public void testGetModePreserve() throws Exception {
    test(
        LINE_JOINER.join(
             "(function() {",
             "function getMode() { return { minified: false } }",
             "var $mode = { getMode: getMode };",
             "  if ($mode.getMode()) {",
             "    console.log('hello world');",
             "  }",
            "})()"),
        LINE_JOINER.join(
             "(function() {",
             "function getMode() { return { minified: false }; }",
             "var $mode = { getMode: getMode };",
             "  if ($mode.getMode()) {",
             "    console.log('hello world');",
             "  }",
            "})()"));
    test(
        LINE_JOINER.join(
             "(function() {",
             "function getMode() { return { otherProp: true } }",
             "var $mode = { getMode: getMode };",
             "  if ($mode.getMode().otherProp) {",
             "    console.log('hello world');",
             "  }",
            "})()"),
        LINE_JOINER.join(
             "(function() {",
             "function getMode() { return { otherProp: true }; }",
             "var $mode = { getMode: getMode };",
             "  if ($mode.getMode().otherProp) {",
             "    console.log('hello world');",
             "  }",
            "})()"));
  }
}
