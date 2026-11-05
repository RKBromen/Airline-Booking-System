import { registerHooks } from 'node:module'

// Node 24 strips TypeScript; resolve the extension-free imports used by Vite.
registerHooks({
  resolve(specifier, context, nextResolve) {
    try { return nextResolve(specifier, context) } catch (error) {
      if (error.code === 'ERR_MODULE_NOT_FOUND' && specifier.startsWith('.') && !/\.(?:[cm]?js|tsx?|json)$/i.test(specifier)) {
        return nextResolve(`${specifier}.ts`, context)
      }
      throw error
    }
  },
})
