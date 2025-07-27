package com.tambapps.marcel.semantic.ast.visitor

import com.tambapps.marcel.semantic.compose.AstStatementScope
import com.tambapps.marcel.semantic.symbol.type.JavaType
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class IsSemanticallyEqualVisitorTest: AstStatementScope() {

  @Test
  fun testPrimitiveValueNodesEquality() {
    // Test BoolNode
    val bool1 = bool(true)
    val bool2 = bool(true)
    val bool3 = bool(false)

    assertTrue(bool1.isSemanticEqualTo(bool2))
    assertFalse(bool1.isSemanticEqualTo(bool3))

    // Test DoubleNode
    val double1 = double(1.5)
    val double2 = double(1.5)
    val double3 = double(2.5)

    assertTrue(double1.isSemanticEqualTo(double2))
    assertFalse(double1.isSemanticEqualTo(double3))

    // Test FloatNode
    val float1 = float(1.5f)
    val float2 = float(1.5f)
    val float3 = float(2.5f)

    assertTrue(float1.isSemanticEqualTo(float2))
    assertFalse(float1.isSemanticEqualTo(float3))

    // Test IntNode
    val int1 = int(42)
    val int2 = int(42)
    val int3 = int(43)

    assertTrue(int1.isSemanticEqualTo(int2))
    assertFalse(int1.isSemanticEqualTo(int3))

    // Test LongNode
    val long1 = long(42L)
    val long2 = long(42L)
    val long3 = long(43L)

    assertTrue(long1.isSemanticEqualTo(long2))
    assertFalse(long1.isSemanticEqualTo(long3))

    // Test NullNode
    val null1 = nullValue()
    val null2 = nullValue()

    assertTrue(null1.isSemanticEqualTo(null2))

    // Test StringNode
    val string1 = string("hello")
    val string2 = string("hello")
    val string3 = string("world")

    assertTrue(string1.isSemanticEqualTo(string2))
    assertFalse(string1.isSemanticEqualTo(string3))

    // Test CharNode
    val char1 = char('a')
    val char2 = char('a')
    val char3 = char('b')

    assertTrue(char1.isSemanticEqualTo(char2))
    assertFalse(char1.isSemanticEqualTo(char3))
  }

  @Test
  fun testDifferentNodeTypesAreNotEqual() {
    val intNode = int(42)
    val boolNode = bool(true)
    val stringNode = string("hello")

    assertFalse(intNode.isSemanticEqualTo(boolNode))
    assertFalse(intNode.isSemanticEqualTo(stringNode))
    assertFalse(boolNode.isSemanticEqualTo(stringNode))
  }

  @Test
  fun testMapEquality() {
    val map1 = map(
      string("key1") to int(1),
      string("key2") to int(2)
    )

    val map2 = map(
      string("key1") to int(1),
      string("key2") to int(2)
    )

    val map3 = map(
      string("key1") to int(1),
      string("different") to int(2)
    )

    val map4 = map(
      string("key1") to int(1),
      string("key2") to int(3) // different value
    )

    assertTrue(map1.isSemanticEqualTo(map2))
    assertFalse(map1.isSemanticEqualTo(map3))
    assertFalse(map1.isSemanticEqualTo(map4))
  }

  @Test
  fun testArrayEquality() {
    val array1 = array(JavaType.intArray, int(1), int(2), int(3))
    val array2 = array(JavaType.intArray, int(1), int(2), int(3))
    val array3 = array(JavaType.intArray, int(1), int(2), int(4)) // different value
    val array4 = array(JavaType.intArray, int(1), int(2)) // different size
    val array5 = array(JavaType.objectArray, int(1), int(2)) // different type

    assertTrue(array1.isSemanticEqualTo(array2))
    assertFalse(array1.isSemanticEqualTo(array3))
    assertFalse(array1.isSemanticEqualTo(array4))
    assertFalse(array1.isSemanticEqualTo(array5))

  }

  /*
  @Test
  fun testMapFilterEquality() {
    val varType = JavaType.int
    val inExpr = array(JavaType.int.arrayType, int(1), int(2), int(3))

    val mapFilter1 = mapFilter(varType, "x", plus(ref("x"), int(1)), gt(ref("x"), int(0)), inExpr)
    val mapFilter2 = mapFilter(varType, "x", plus(ref("x"), int(1)), gt(ref("x"), int(0)), inExpr)
    val mapFilter3 = mapFilter(varType, "y", plus(ref("y"), int(1)), gt(ref("y"), int(0)), inExpr) // different variable name
    val mapFilter4 = mapFilter(varType, "x", plus(ref("x"), int(2)), gt(ref("x"), int(0)), inExpr) // different map expression

    assertTrue(mapFilter1.isSemanticEqualTo(mapFilter2))
    assertFalse(mapFilter1.isSemanticEqualTo(mapFilter3))
    assertFalse(mapFilter1.isSemanticEqualTo(mapFilter4))
  }

  @Test
  fun testAllInEquality() {
    val varType = JavaType.int
    val inExpr = array(int(1), int(2), int(3))

    val allIn1 = allIn(varType, "x", gt(ref("x"), int(0)), inExpr)
    val allIn2 = allIn(varType, "x", gt(ref("x"), int(0)), inExpr)
    val allIn3 = allIn(varType, "y", gt(ref("y"), int(0)), inExpr) // different variable name
    val allIn4 = allIn(varType, "x", gt(ref("x"), int(1)), inExpr) // different filter expression
    val allIn5 = allIn(varType, "x", gt(ref("x"), int(0)), inExpr, true) // with negate = true

    assertTrue(allIn1.isSemanticEqualTo(allIn2))
    assertFalse(allIn1.isSemanticEqualTo(allIn3))
    assertFalse(allIn1.isSemanticEqualTo(allIn4))
    assertFalse(allIn1.isSemanticEqualTo(allIn5))
  }

  @Test
  fun testAnyInEquality() {
    val varType = type("int")
    val inExpr = array(int(1), int(2), int(3))

    val anyIn1 = anyIn(varType, "x", gt(ref("x"), int(0)), inExpr)
    val anyIn2 = anyIn(varType, "x", gt(ref("x"), int(0)), inExpr)
    val anyIn3 = anyIn(varType, "y", gt(ref("y"), int(0)), inExpr) // different variable name
    val anyIn4 = anyIn(varType, "x", gt(ref("x"), int(1)), inExpr) // different filter expression
    val anyIn5 = anyIn(varType, "x", gt(ref("x"), int(0)), inExpr, true) // with negate = true

    assertTrue(anyIn1.isSemanticEqualTo(anyIn2))
    assertFalse(anyIn1.isSemanticEqualTo(anyIn3))
    assertFalse(anyIn1.isSemanticEqualTo(anyIn4))
    assertFalse(anyIn1.isSemanticEqualTo(anyIn5))
  }

  @Test
  fun testFindInEquality() {
    val varType = JavaType.int
    val inExpr = array(int(1), int(2), int(3))

    val findIn1 = findIn(varType, "x", gt(ref("x"), int(0)), inExpr)
    val findIn2 = findIn(varType, "x", gt(ref("x"), int(0)), inExpr)
    val findIn3 = findIn(varType, "y", gt(ref("y"), int(0)), inExpr) // different variable name
    val findIn4 = findIn(varType, "x", gt(ref("x"), int(1)), inExpr) // different filter expression

    assertTrue(findIn1.isSemanticEqualTo(findIn2))
    assertFalse(findIn1.isSemanticEqualTo(findIn3))
    assertFalse(findIn1.isSemanticEqualTo(findIn4))
  }
   */

  @Test
  fun testNotEquality() {
    val not1 = not(bool(true))
    val not2 = not(bool(true))
    val not3 = not(bool(false))

    assertTrue(not1.isSemanticEqualTo(not2))
    assertFalse(not1.isSemanticEqualTo(not3))
  }

  @Test
  fun testBinaryOperatorEquality() {
    val binOp1 = plus(int(1), int(2))
    val binOp2 = plus(int(1), int(2))
    val binOp3 = plus(int(1), int(3)) // different right operand
    val binOp4 = plus(int(2), int(2)) // different left operand
    val binOp5 = minus(int(1), int(2)) // different operator

    assertTrue(binOp1.isSemanticEqualTo(binOp2))
    assertFalse(binOp1.isSemanticEqualTo(binOp3))
    assertFalse(binOp1.isSemanticEqualTo(binOp4))
    assertFalse(binOp1.isSemanticEqualTo(binOp5))
  }

  /*
  @Test
  fun testElvisThrowEquality() {
    val elvisThrow1 = elvisThrow(ref("x"), new(type("Exception")))
    val elvisThrow2 = elvisThrow(ref("x"), new(type("Exception")))
    val elvisThrow3 = elvisThrow(ref("y"), new(type("Exception"))) // different expression
    val elvisThrow4 = elvisThrow(ref("x"), new(type("RuntimeException"))) // different throwable

    assertTrue(elvisThrow1.isSemanticEqualTo(elvisThrow2))
    assertFalse(elvisThrow1.isSemanticEqualTo(elvisThrow3))
    assertFalse(elvisThrow1.isSemanticEqualTo(elvisThrow4))
  }
   */

  @Test
  fun testBinaryTypeOperatorEquality() {
    val binTypeOp1 = instanceof(string("foo"), JavaType.String)
    val binTypeOp2 = instanceof(string("foo"), JavaType.String)
    val binTypeOp3 = instanceof(string("bar"), JavaType.String) // different left operand
    val binTypeOp4 = instanceof(string("foo"), JavaType.int) // different right operand

    assertTrue(binTypeOp1.isSemanticEqualTo(binTypeOp2))
    assertFalse(binTypeOp1.isSemanticEqualTo(binTypeOp3))
    assertFalse(binTypeOp1.isSemanticEqualTo(binTypeOp4))
  }

  @Test
  fun testTernaryEquality() {
    val variableX = lv(JavaType.int, "x")
    val variableY = lv(JavaType.int, "y")
    val ternary1 = ternary(eq(ref(variableX), int(0)), string("zero"), string("not zero"))
    val ternary2 = ternary(eq(ref(variableX), int(0)), string("zero"), string("not zero"))
    val ternary3 = ternary(eq(ref(variableY), int(0)), string("zero"), string("not zero")) // different test
    val ternary4 = ternary(eq(ref(variableX), int(0)), string("0"), string("not zero")) // different true expr
    val ternary5 = ternary(eq(ref(variableX), int(0)), string("zero"), string("nonzero")) // different false expr

    assertTrue(ternary1.isSemanticEqualTo(ternary2))
    assertFalse(ternary1.isSemanticEqualTo(ternary3))
    assertFalse(ternary1.isSemanticEqualTo(ternary4))
    assertFalse(ternary1.isSemanticEqualTo(ternary5))
  }

  @Test
  fun testClassReferenceEquality() {
    val classRef1 = classRef(JavaType.String)
    val classRef2 = classRef(JavaType.String)
    val classRef3 = classRef(JavaType.int)

    assertTrue(classRef1.isSemanticEqualTo(classRef2))
    assertFalse(classRef1.isSemanticEqualTo(classRef3))
  }

  @Test
  fun testThisAndSuperReferenceEquality() {
    val thisRef1 = thisRef(JavaType.IntRange)
    val thisRef2 = thisRef(JavaType.IntRange)
    val superRef1 = superRef(JavaType.IntRange)
    val superRef2 = superRef(JavaType.IntRange)

    assertTrue(thisRef1.isSemanticEqualTo(thisRef2))
    assertTrue(superRef1.isSemanticEqualTo(superRef2))
    assertFalse(thisRef1.isSemanticEqualTo(superRef1))
  }

  @Test
  fun testArrayAccessEquality() {
    val indexAccess1 = arrayAccess(ref(lv(JavaType.intArray, "array")), int(0))
    val indexAccess2 = arrayAccess(ref(lv(JavaType.intArray, "array")), int(0))
    val indexAccess3 = arrayAccess(ref(lv(JavaType.intArray, "list")), int(0)) // different owner
    val indexAccess4 = arrayAccess(ref(lv(JavaType.intArray, "array")), int(1)) // different index

    assertTrue(indexAccess1.isSemanticEqualTo(indexAccess2))
    assertFalse(indexAccess1.isSemanticEqualTo(indexAccess3))
    assertFalse(indexAccess1.isSemanticEqualTo(indexAccess4))
  }

  @Test
  fun testReferenceEquality() {
    val ref1 = ref(lv(JavaType.int, "variable"))
    val ref2 = ref(lv(JavaType.int, "variable"))
    val ref3 = ref(lv(JavaType.int, "anotherVariable"))

    assertTrue(ref1.isSemanticEqualTo(ref2))
    assertFalse(ref1.isSemanticEqualTo(ref3))
  }

  /*
  @Test
  fun testFunctionCallEquality() {
    val funcCall1 = fCall("function", listOf(int(1), string("hello")))
    val funcCall2 = fCall("function", listOf(int(1), string("hello")))
    val funcCall3 = fCall("anotherFunction", listOf(int(1), string("hello"))) // different name
    val funcCall4 = fCall("function", listOf(int(2), string("hello"))) // different args
    val funcCall5 = fCall("function", listOf(int(1))) // different number of args

    val namedFuncCall1 = fCall("function", namedArgs = listOf("arg1" to int(1), "arg2" to string("hello")))
    val namedFuncCall2 = fCall("function", namedArgs = listOf("arg1" to int(1), "arg2" to string("hello")))
    val namedFuncCall3 = fCall("function", namedArgs = listOf("different" to int(1), "arg2" to string("hello"))) // different name
    val namedFuncCall4 = fCall("function", namedArgs = listOf("arg1" to int(2), "arg2" to string("hello"))) // different value

    assertTrue(funcCall1.isSemanticEqualTo(funcCall2))
    assertFalse(funcCall1.isSemanticEqualTo(funcCall3))
    assertFalse(funcCall1.isSemanticEqualTo(funcCall4))
    assertFalse(funcCall1.isSemanticEqualTo(funcCall5))

    assertTrue(namedFuncCall1.isSemanticEqualTo(namedFuncCall2))
    assertFalse(namedFuncCall1.isSemanticEqualTo(namedFuncCall3))
    assertFalse(namedFuncCall1.isSemanticEqualTo(namedFuncCall4))
  }

  @Test
  fun testConstructorCallEquality() {
    val superConstrCall1 = superConstrCall(listOf(int(1), string("hello")))
    val superConstrCall2 = superConstrCall(listOf(int(1), string("hello")))
    val superConstrCall3 = superConstrCall(listOf(int(2), string("hello"))) // different args

    val thisConstrCall1 = thisConstrCall(listOf(int(1), string("hello")))
    val thisConstrCall2 = thisConstrCall(listOf(int(1), string("hello")))
    val thisConstrCall3 = thisConstrCall(listOf(int(2), string("hello"))) // different args

    assertTrue(superConstrCall1.isSemanticEqualTo(superConstrCall2))
    assertFalse(superConstrCall1.isSemanticEqualTo(superConstrCall3))
    assertTrue(thisConstrCall1.isSemanticEqualTo(thisConstrCall2))
    assertFalse(thisConstrCall1.isSemanticEqualTo(thisConstrCall3))
    assertFalse(superConstrCall1.isSemanticEqualTo(thisConstrCall1))
  }

  @Test
  fun testNewInstanceEquality() {
    val newInstance1 = new(type("MyClass"), listOf(int(1), string("hello")))
    val newInstance2 = new(type("MyClass"), listOf(int(1), string("hello")))
    val newInstance3 = new(type("OtherClass"), listOf(int(1), string("hello"))) // different class
    val newInstance4 = new(type("MyClass"), listOf(int(2), string("hello"))) // different args

    val namedNewInstance1 = new(type("MyClass"), namedArgs = listOf("arg1" to int(1), "arg2" to string("hello")))
    val namedNewInstance2 = new(type("MyClass"), namedArgs = listOf("arg1" to int(1), "arg2" to string("hello")))
    val namedNewInstance3 = new(type("MyClass"), namedArgs = listOf("different" to int(1), "arg2" to string("hello"))) // different name

    assertTrue(newInstance1.isSemanticEqualTo(newInstance2))
    assertFalse(newInstance1.isSemanticEqualTo(newInstance3))
    assertFalse(newInstance1.isSemanticEqualTo(newInstance4))

    assertTrue(namedNewInstance1.isSemanticEqualTo(namedNewInstance2))
    assertFalse(namedNewInstance1.isSemanticEqualTo(namedNewInstance3))
  }

  @Test
  fun testWhenAndSwitchEquality() {
    val whenNode1 = whenExpr {
      branch(eq(ref("x"), int(0))) {
        stmt(string("zero"))
      }
      branch(eq(ref("x"), int(1))) {
        stmt(string("one"))
      }
      elseBranch { stmt(string("other")) }
    }

    val whenNode2 = whenExpr {
      branch(eq(ref("x"), int(0))) {
        stmt(string("zero"))
      }
      branch(eq(ref("x"), int(1))) {
        stmt(string("one"))
      }
      elseBranch { stmt(string("other")) }
    }

    val whenNode3 = whenExpr {
      branch(eq(ref("x"), int(0))) {
        stmt(string("zero"))
      }
      branch(eq(ref("x"), int(2))) { // different condition
        stmt(string("one"))
      }
      elseBranch { stmt(string("other")) }
    }

    val whenNode4 = whenExpr {
      branch(eq(ref("x"), int(0))) {
        stmt(string("zero"))
      }
      branch(eq(ref("x"), int(1))) {
        stmt(string("ONE")) // different value
      }
      elseBranch { stmt(string("other")) }
    }

    val switchNode1 = switchExpr(ref("value")) {
      branch(int(0)) {
        stmt(string("zero"))
      }
      branch(int(1)) {
        stmt(string("one"))
      }
      elseBranch { stmt(string("other")) }
    }

    val switchNode2 = switchExpr(ref("value")) {
      branch(int(0)) {
        stmt(string("zero"))
      }
      branch(int(1)) {
        stmt(string("one"))
      }
      elseBranch { stmt(string("other")) }
    }

    val switchNode3 = switchExpr(ref("other")) { // different switch expression
      branch(int(0)) {
        stmt(string("zero"))
      }
      branch(int(1)) {
        stmt(string("one"))
      }
      elseBranch { stmt(string("other")) }
    }

    assertTrue(whenNode1.isSemanticEqualTo(whenNode2))
    assertFalse(whenNode1.isSemanticEqualTo(whenNode3))
    assertFalse(whenNode1.isSemanticEqualTo(whenNode4))

    assertTrue(switchNode1.isSemanticEqualTo(switchNode2))
    assertFalse(switchNode1.isSemanticEqualTo(switchNode3))
    assertFalse(whenNode1.isSemanticEqualTo(switchNode1)) // when and switch are different
  }

  @Test
  fun testAsyncBlockEquality() {
    val asyncBlock1 = async {
      stmt(fCall("println", listOf(string("hello"))))
      returnStmt(int(42))
    }

    val asyncBlock2 = async {
      stmt(fCall("println", listOf(string("hello"))))
      returnStmt(int(42))
    }

    val asyncBlock3 = async {
      stmt(fCall("println", listOf(string("world"))))
      returnStmt(int(42))
    }

    assertTrue(asyncBlock1.isSemanticEqualTo(asyncBlock2))
    assertFalse(asyncBlock1.isSemanticEqualTo(asyncBlock3))
  }

  @Test
  fun testTruthyVariableDeclarationEquality() {
    val truthyVar1 = truthyVarDecl(type("boolean"), "result", eq(ref("x"), int(0)))
    val truthyVar2 = truthyVarDecl(type("boolean"), "result", eq(ref("x"), int(0)))
    val truthyVar3 = truthyVarDecl(type("boolean"), "different", eq(ref("x"), int(0))) // different name
    val truthyVar4 = truthyVarDecl(type("boolean"), "result", eq(ref("x"), int(1))) // different expression

    assertTrue(truthyVar1.isSemanticEqualTo(truthyVar2))
    assertFalse(truthyVar1.isSemanticEqualTo(truthyVar3))
    assertFalse(truthyVar1.isSemanticEqualTo(truthyVar4))
  }

  @Test
  fun testStatementNodesEquality() {
    // Test ExpressionStatementNode
    val exprStmt1 = stmt(fCall("println", listOf(string("hello"))))
    val exprStmt2 = stmt(fCall("println", listOf(string("hello"))))
    val exprStmt3 = stmt(fCall("println", listOf(string("world"))))

    assertTrue(exprStmt1.isSemanticEqualTo(exprStmt2))
    assertFalse(exprStmt1.isSemanticEqualTo(exprStmt3))

    // Test ReturnNode
    val returnStmt1 = returnStmt(int(42))
    val returnStmt2 = returnStmt(int(42))
    val returnStmt3 = returnStmt(int(43))

    assertTrue(returnStmt1.isSemanticEqualTo(returnStmt2))
    assertFalse(returnStmt1.isSemanticEqualTo(returnStmt3))

    // Test VariableDeclarationNode
    val varDecl1 = varDeclStmt(type("int"), "x", int(42))
    val varDecl2 = varDeclStmt(type("int"), "x", int(42))
    val varDecl3 = varDeclStmt(type("int"), "y", int(42)) // different name
    val varDecl4 = varDeclStmt(type("int"), "x", int(43)) // different value
    val varDecl5 = varDeclStmt(type("String"), "x", int(42)) // different type

    assertTrue(varDecl1.isSemanticEqualTo(varDecl2))
    assertFalse(varDecl1.isSemanticEqualTo(varDecl3))
    assertFalse(varDecl1.isSemanticEqualTo(varDecl4))
    assertFalse(varDecl1.isSemanticEqualTo(varDecl5))

    // Test MultiVarDeclarationNode
    val multiVarDecl1 = multiVarDeclStmt(
      listOf(
        Triple(type("int"), "x", false),
        Triple(type("String"), "y", true)
      ),
      fCall("getValues")
    )

    val multiVarDecl2 = multiVarDeclStmt(
      listOf(
        Triple(type("int"), "x", false),
        Triple(type("String"), "y", true)
      ),
      fCall("getValues")
    )

    val multiVarDecl3 = multiVarDeclStmt(
      listOf(
        Triple(type("int"), "x", false),
        Triple(type("String"), "z", true) // different name
      ),
      fCall("getValues")
    )

    assertTrue(multiVarDecl1.isSemanticEqualTo(multiVarDecl2))
    assertFalse(multiVarDecl1.isSemanticEqualTo(multiVarDecl3))

    // Test IfStatementNode
    val ifStmt1 = ifStmt(eq(ref("x"), int(0))) {
      trueBlock { stmt(fCall("println", listOf(string("zero")))) }
      falseBlock { stmt(fCall("println", listOf(string("not zero")))) }
    }

    val ifStmt2 = ifStmt(eq(ref("x"), int(0))) {
      trueBlock { stmt(fCall("println", listOf(string("zero")))) }
      falseBlock { stmt(fCall("println", listOf(string("not zero")))) }
    }

    val ifStmt3 = ifStmt(eq(ref("y"), int(0))) { // different condition
      trueBlock { stmt(fCall("println", listOf(string("zero")))) }
      falseBlock { stmt(fCall("println", listOf(string("not zero")))) }
    }

    assertTrue(ifStmt1.isSemanticEqualTo(ifStmt2))
    assertFalse(ifStmt1.isSemanticEqualTo(ifStmt3))

    // Test ForInNode
    val forInStmt1 = forInStmt(type("int"), "i", false, array(int(1), int(2), int(3))) {
      stmt(fCall("println", listOf(ref("i"))))
    }

    val forInStmt2 = forInStmt(type("int"), "i", false, array(int(1), int(2), int(3))) {
      stmt(fCall("println", listOf(ref("i"))))
    }

    val forInStmt3 = forInStmt(type("int"), "j", false, array(int(1), int(2), int(3))) { // different var name
      stmt(fCall("println", listOf(ref("j"))))
    }

    assertTrue(forInStmt1.isSemanticEqualTo(forInStmt2))
    assertFalse(forInStmt1.isSemanticEqualTo(forInStmt3))

    // Test BlockNode
    val block1 = block {
      stmt(fCall("println", listOf(string("first"))))
      stmt(fCall("println", listOf(string("second"))))
    }

    val block2 = block {
      stmt(fCall("println", listOf(string("first"))))
      stmt(fCall("println", listOf(string("second"))))
    }

    val block3 = block {
      stmt(fCall("println", listOf(string("first"))))
      stmt(fCall("println", listOf(string("different"))))
    }

    assertTrue(block1.isSemanticEqualTo(block2))
    assertFalse(block1.isSemanticEqualTo(block3))

    // Test BreakNode and ContinueNode
    val breakStmt1 = breakStmt()
    val breakStmt2 = breakStmt()
    val continueStmt1 = continueStmt()
    val continueStmt2 = continueStmt()

    assertTrue(breakStmt1.isSemanticEqualTo(breakStmt2))
    assertTrue(continueStmt1.isSemanticEqualTo(continueStmt2))
    assertFalse(breakStmt1.isSemanticEqualTo(continueStmt1))

    // Test ThrowNode
    val throwStmt1 = throwStmt(new(type("Exception"), listOf(string("error"))))
    val throwStmt2 = throwStmt(new(type("Exception"), listOf(string("error"))))
    val throwStmt3 = throwStmt(new(type("Exception"), listOf(string("different"))))

    assertTrue(throwStmt1.isSemanticEqualTo(throwStmt2))
    assertFalse(throwStmt1.isSemanticEqualTo(throwStmt3))

    // Test TryCatchNode
    val tryCatchStmt1 = tryCatchStmt {
      tryBlock { stmt(fCall("riskyOperation")) }
      catchBlock(listOf(type("Exception")), "e") { stmt(fCall("handleError")) }
    }

    val tryCatchStmt2 = tryCatchStmt {
      tryBlock { stmt(fCall("riskyOperation")) }
      catchBlock(listOf(type("Exception")), "e") { stmt(fCall("handleError")) }
    }

    val tryCatchStmt3 = tryCatchStmt {
      tryBlock { stmt(fCall("differentOperation")) }
      catchBlock(listOf(type("Exception")), "e") { stmt(fCall("handleError")) }
    }

    assertTrue(tryCatchStmt1.isSemanticEqualTo(tryCatchStmt2))
    assertFalse(tryCatchStmt1.isSemanticEqualTo(tryCatchStmt3))
  }
   */

}