// The page imports the bundle as an ES module, so webpack cannot infer the
// public path from document.currentScript; load chunks relative to the page.
config.output = config.output || {};
config.output.publicPath = "";
