(function (root, factory) {
  if (typeof define === 'function' && define.amd)
    define(['exports', 'kotlin'], factory);
  else if (typeof exports === 'object')
    factory(module.exports, require('kotlin'));
  else {
    if (typeof kotlin === 'undefined') {
      throw new Error("Error loading module 'ci-pipeline'. Its dependency 'kotlin' was not found. Please, check whether 'kotlin' is loaded prior to 'ci-pipeline'.");
    }
    root['ci-pipeline'] = factory(typeof this['ci-pipeline'] === 'undefined' ? {} : this['ci-pipeline'], kotlin);
  }
}(this, function (_, Kotlin) {
  'use strict';
  var println = Kotlin.kotlin.io.println_s8jyv4$;
  function main() {
    println(greeting('ci-pipeline'));
  }
  function greeting(name) {
    return 'Hello weird, ' + name;
  }
  _.main = main;
  _.greeting_61zpoe$ = greeting;
  main();
  Kotlin.defineModule('ci-pipeline', _);
  return _;
}));

//# sourceMappingURL=ci-pipeline.js.map
