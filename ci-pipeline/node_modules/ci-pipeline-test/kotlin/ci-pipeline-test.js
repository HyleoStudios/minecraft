(function (root, factory) {
  if (typeof define === 'function' && define.amd)
    define(['exports', 'kotlin', 'ci-pipeline', 'kotlin-test'], factory);
  else if (typeof exports === 'object')
    factory(module.exports, require('kotlin'), require('ci-pipeline'), require('kotlin-test'));
  else {
    if (typeof kotlin === 'undefined') {
      throw new Error("Error loading module 'ci-pipeline-test'. Its dependency 'kotlin' was not found. Please, check whether 'kotlin' is loaded prior to 'ci-pipeline-test'.");
    }
    if (typeof this['ci-pipeline'] === 'undefined') {
      throw new Error("Error loading module 'ci-pipeline-test'. Its dependency 'ci-pipeline' was not found. Please, check whether 'ci-pipeline' is loaded prior to 'ci-pipeline-test'.");
    }
    if (typeof this['kotlin-test'] === 'undefined') {
      throw new Error("Error loading module 'ci-pipeline-test'. Its dependency 'kotlin-test' was not found. Please, check whether 'kotlin-test' is loaded prior to 'ci-pipeline-test'.");
    }
    root['ci-pipeline-test'] = factory(typeof this['ci-pipeline-test'] === 'undefined' ? {} : this['ci-pipeline-test'], kotlin, this['ci-pipeline'], this['kotlin-test']);
  }
}(this, function (_, Kotlin, $module$ci_pipeline, $module$kotlin_test) {
  'use strict';
  var greeting = $module$ci_pipeline.greeting_61zpoe$;
  var assertEquals = $module$kotlin_test.kotlin.test.assertEquals_3m0tl5$;
  var Kind_CLASS = Kotlin.Kind.CLASS;
  var test = $module$kotlin_test.kotlin.test.test;
  var suite = $module$kotlin_test.kotlin.test.suite;
  function GreetingTest() {
  }
  GreetingTest.prototype.testGreeting = function () {
    assertEquals(greeting('World'), 'Hello, World');
  };
  GreetingTest.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'GreetingTest',
    interfaces: []
  };
  _.GreetingTest = GreetingTest;
  suite('', false, function () {
    suite('GreetingTest', false, function () {
      test('testGreeting', false, function () {
        return (new GreetingTest()).testGreeting();
      });
    });
  });
  Kotlin.defineModule('ci-pipeline-test', _);
  return _;
}));

//# sourceMappingURL=ci-pipeline-test.js.map
