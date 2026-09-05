package com.github.theredbrain.bundleapi.client;

public class BundleAPIClient {
	public static void init() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		// Item model properties (bundleapi:custom_bundle/fullness) are registered from
		// NumericPropertiesMixin, because vanilla bootstraps them before mod client init runs.
	}
}
