package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.parser.compose.CstStatementScope
import com.tambapps.marcel.parser.cst.expression.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class IsEqualVisitorTest : CstStatementScope() {

  @Test
  fun testPrimitiveValueNodesEquality() {
    // Test BoolCstNode
    val bool1 = bool(true)
    val bool2 = bool(true)
    val bool3 = bool(false)

    assertTrue(bool1.isEqualTo(bool2))
    assertFalse(bool1.isEqualTo(bool3))

    // Test DoubleCstNode
    val double1 = double(1.5)
    val double2 = double(1.5)
    val double3 = double(2.5)

    assertTrue(double1.isEqualTo(double2))
    assertFalse(double1.isEqualTo(double3))

    // Test FloatCstNode
    val float1 = float(1.5f)
    val float2 = float(1.5f)
    val float3 = float(2.5f)

    assertTrue(float1.isEqualTo(float2))
    assertFalse(float1.isEqualTo(float3))

    // Test IntCstNode
    val int1 = int(42)
    val int2 = int(42)
    val int3 = int(43)

    assertTrue(int1.isEqualTo(int2))
    assertFalse(int1.isEqualTo(int3))

    // Test LongCstNode
    val long1 = long(42L)
    val long2 = long(42L)
    val long3 = long(43L)

    assertTrue(long1.isEqualTo(long2))
    assertFalse(long1.isEqualTo(long3))

    // Test NullCstNode
    val null1 = nullValue()
    val null2 = nullValue()

    assertTrue(null1.isEqualTo(null2))

    // Test StringCstNode
    val string1 = string("hello")
    val string2 = string("hello")
    val string3 = string("world")

    assertTrue(string1.isEqualTo(string2))
    assertFalse(string1.isEqualTo(string3))

    // Test RegexCstNode
    val regex1 = regex("[a-z]+", "i")
    val regex2 = regex("[a-z]+", "i")
    val regex3 = regex("[a-z]+", "l")
    val regex4 = regex("[0-9]+", "i")

    assertTrue(regex1.isEqualTo(regex2))
    assertFalse(regex1.isEqualTo(regex3))
    assertFalse(regex1.isEqualTo(regex4))

    // Test CharCstNode
    val char1 = char('a')
    val char2 = char('a')
    val char3 = char('b')

    assertTrue(char1.isEqualTo(char2))
    assertFalse(char1.isEqualTo(char3))
  }

  @Test
  fun testDifferentNodeTypesAreNotEqual() {
    val intNode = int(42)
    val boolNode = bool(true)
    val stringNode = string("hello")

    assertFalse(intNode.isEqualTo(boolNode))
    assertFalse(intNode.isEqualTo(stringNode))
    assertFalse(boolNode.isEqualTo(stringNode))
  }

  @Test
  fun testTemplateStringEquality() {
    val template1 = templateSting("hello")
    val template2 = templateSting("hello")
    val template3 = templateSting("world")

    assertTrue(template1.isEqualTo(template2))
    assertFalse(template1.isEqualTo(template3))
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

    assertTrue(map1.isEqualTo(map2))
    assertFalse(map1.isEqualTo(map3))
    assertFalse(map1.isEqualTo(map4))
  }

  @Test
  fun testArrayEquality() {
    val array1 = array(int(1), int(2), int(3))
    val array2 = array(int(1), int(2), int(3))
    val array3 = array(int(1), int(2), int(4)) // different value
    val array4 = array(int(1), int(2)) // different size

    assertTrue(array1.isEqualTo(array2))
    assertFalse(array1.isEqualTo(array3))
    assertFalse(array1.isEqualTo(array4))
  }

  @Test
  fun testMapFilterEquality() {
    val varType = type("int")
    val inExpr = array(int(1), int(2), int(3))

    val mapFilter1 = mapFilter(varType, "x", plus(ref("x"), int(1)), gt(ref("x"), int(0)), inExpr)
    val mapFilter2 = mapFilter(varType, "x", plus(ref("x"), int(1)), gt(ref("x"), int(0)), inExpr)
    val mapFilter3 = mapFilter(varType, "y", plus(ref("y"), int(1)), gt(ref("y"), int(0)), inExpr) // different variable name
    val mapFilter4 = mapFilter(varType, "x", plus(ref("x"), int(2)), gt(ref("x"), int(0)), inExpr) // different map expression

    assertTrue(mapFilter1.isEqualTo(mapFilter2))
    assertFalse(mapFilter1.isEqualTo(mapFilter3))
    assertFalse(mapFilter1.isEqualTo(mapFilter4))
  }

  @Test
  fun testAllInEquality() {
    val varType = type("int")
    val inExpr = array(int(1), int(2), int(3))

    val allIn1 = allIn(varType, "x", gt(ref("x"), int(0)), inExpr)
    val allIn2 = allIn(varType, "x", gt(ref("x"), int(0)), inExpr)
    val allIn3 = allIn(varType, "y", gt(ref("y"), int(0)), inExpr) // different variable name
    val allIn4 = allIn(varType, "x", gt(ref("x"), int(1)), inExpr) // different filter expression
    val allIn5 = allIn(varType, "x", gt(ref("x"), int(0)), inExpr, true) // with negate = true

    assertTrue(allIn1.isEqualTo(allIn2))
    assertFalse(allIn1.isEqualTo(allIn3))
    assertFalse(allIn1.isEqualTo(allIn4))
    assertFalse(allIn1.isEqualTo(allIn5))
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

    assertTrue(anyIn1.isEqualTo(anyIn2))
    assertFalse(anyIn1.isEqualTo(anyIn3))
    assertFalse(anyIn1.isEqualTo(anyIn4))
    assertFalse(anyIn1.isEqualTo(anyIn5))
  }

  @Test
  fun testFindInEquality() {
    val varType = type("int")
    val inExpr = array(int(1), int(2), int(3))

    val findIn1 = findIn(varType, "x", gt(ref("x"), int(0)), inExpr)
    val findIn2 = findIn(varType, "x", gt(ref("x"), int(0)), inExpr)
    val findIn3 = findIn(varType, "y", gt(ref("y"), int(0)), inExpr) // different variable name
    val findIn4 = findIn(varType, "x", gt(ref("x"), int(1)), inExpr) // different filter expression

    assertTrue(findIn1.isEqualTo(findIn2))
    assertFalse(findIn1.isEqualTo(findIn3))
    assertFalse(findIn1.isEqualTo(findIn4))
  }

  @Test
  fun testUnaryMinusEquality() {
    val minus1 = minus(int(42))
    val minus2 = minus(int(42))
    val minus3 = minus(int(43))

    assertTrue(minus1.isEqualTo(minus2))
    assertFalse(minus1.isEqualTo(minus3))
  }

  @Test
  fun testNotEquality() {
    val not1 = not(bool(true))
    val not2 = not(bool(true))
    val not3 = not(bool(false))

    assertTrue(not1.isEqualTo(not2))
    assertFalse(not1.isEqualTo(not3))
  }

  @Test
  fun testWrappedExpressionEquality() {
    val wrapped1 = WrappedExpressionCstNode(int(42))
    val wrapped2 = WrappedExpressionCstNode(int(42))
    val wrapped3 = WrappedExpressionCstNode(int(43))

    assertTrue(wrapped1.isEqualTo(wrapped2))
    assertFalse(wrapped1.isEqualTo(wrapped3))
  }

  @Test
  fun testBinaryOperatorEquality() {
    val binOp1 = plus(int(1), int(2))
    val binOp2 = plus(int(1), int(2))
    val binOp3 = plus(int(1), int(3)) // different right operand
    val binOp4 = plus(int(2), int(2)) // different left operand
    val binOp5 = minus(int(1), int(2)) // different operator

    assertTrue(binOp1.isEqualTo(binOp2))
    assertFalse(binOp1.isEqualTo(binOp3))
    assertFalse(binOp1.isEqualTo(binOp4))
    assertFalse(binOp1.isEqualTo(binOp5))
  }

  @Test
  fun testElvisThrowEquality() {
    val elvisThrow1 = elvisThrow(ref("x"), new(type("Exception")))
    val elvisThrow2 = elvisThrow(ref("x"), new(type("Exception")))
    val elvisThrow3 = elvisThrow(ref("y"), new(type("Exception"))) // different expression
    val elvisThrow4 = elvisThrow(ref("x"), new(type("RuntimeException"))) // different throwable

    assertTrue(elvisThrow1.isEqualTo(elvisThrow2))
    assertFalse(elvisThrow1.isEqualTo(elvisThrow3))
    assertFalse(elvisThrow1.isEqualTo(elvisThrow4))
  }

  @Test
  fun testBinaryTypeOperatorEquality() {
    val binTypeOp1 = instanceof(ref("x"), type("String"))
    val binTypeOp2 = instanceof(ref("x"), type("String"))
    val binTypeOp3 = instanceof(ref("y"), type("String")) // different left operand
    val binTypeOp4 = instanceof(ref("x"), type("Integer")) // different right operand
    val binTypeOp5 = asType(ref("x"), type("String")) // different operator

    assertTrue(binTypeOp1.isEqualTo(binTypeOp2))
    assertFalse(binTypeOp1.isEqualTo(binTypeOp3))
    assertFalse(binTypeOp1.isEqualTo(binTypeOp4))
    assertFalse(binTypeOp1.isEqualTo(binTypeOp5))
  }

  @Test
  fun testTernaryEquality() {
    val ternary1 = ternary(eq(ref("x"), int(0)), string("zero"), string("not zero"))
    val ternary2 = ternary(eq(ref("x"), int(0)), string("zero"), string("not zero"))
    val ternary3 = ternary(eq(ref("y"), int(0)), string("zero"), string("not zero")) // different test
    val ternary4 = ternary(eq(ref("x"), int(0)), string("0"), string("not zero")) // different true expr
    val ternary5 = ternary(eq(ref("x"), int(0)), string("zero"), string("nonzero")) // different false expr

    assertTrue(ternary1.isEqualTo(ternary2))
    assertFalse(ternary1.isEqualTo(ternary3))
    assertFalse(ternary1.isEqualTo(ternary4))
    assertFalse(ternary1.isEqualTo(ternary5))
  }

  @Test
  fun testClassReferenceEquality() {
    val classRef1 = classReference(type("String"))
    val classRef2 = classReference(type("String"))
    val classRef3 = classReference(type("Integer"))

    assertTrue(classRef1.isEqualTo(classRef2))
    assertFalse(classRef1.isEqualTo(classRef3))
  }

  @Test
  fun testThisAndSuperReferenceEquality() {
    val thisRef1 = thisRef()
    val thisRef2 = thisRef()
    val superRef1 = superRef()
    val superRef2 = superRef()

    assertTrue(thisRef1.isEqualTo(thisRef2))
    assertTrue(superRef1.isEqualTo(superRef2))
    assertFalse(thisRef1.isEqualTo(superRef1))
  }

  @Test
  fun testDirectFieldReferenceEquality() {
    val fieldRef1 = directFieldRef("field1")
    val fieldRef2 = directFieldRef("field1")
    val fieldRef3 = directFieldRef("field2")

    assertTrue(fieldRef1.isEqualTo(fieldRef2))
    assertFalse(fieldRef1.isEqualTo(fieldRef3))
  }

  @Test
  fun testIncrEquality() {
    val incr1 = incr("i", true)
    val incr2 = incr("i", true)
    val incr3 = incr("j", true) // different variable
    val incr4 = incr("i", false) // different returnValueBefore
    val incr5 = incr("i", true, 2) // different amount

    assertTrue(incr1.isEqualTo(incr2))
    assertFalse(incr1.isEqualTo(incr3))
    assertFalse(incr1.isEqualTo(incr4))
    assertFalse(incr1.isEqualTo(incr5))
  }

  @Test
  fun testIndexAccessEquality() {
    val indexAccess1 = indexAccess(ref("array"), listOf(int(0)))
    val indexAccess2 = indexAccess(ref("array"), listOf(int(0)))
    val indexAccess3 = indexAccess(ref("list"), listOf(int(0))) // different owner
    val indexAccess4 = indexAccess(ref("array"), listOf(int(1))) // different index
    val indexAccess5 = indexAccess(ref("array"), listOf(int(0)), true) // safe access

    assertTrue(indexAccess1.isEqualTo(indexAccess2))
    assertFalse(indexAccess1.isEqualTo(indexAccess3))
    assertFalse(indexAccess1.isEqualTo(indexAccess4))
    assertFalse(indexAccess1.isEqualTo(indexAccess5))
  }

  @Test
  fun testReferenceEquality() {
    val ref1 = ref("variable")
    val ref2 = ref("variable")
    val ref3 = ref("anotherVariable")

    assertTrue(ref1.isEqualTo(ref2))
    assertFalse(ref1.isEqualTo(ref3))
  }

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

    assertTrue(funcCall1.isEqualTo(funcCall2))
    assertFalse(funcCall1.isEqualTo(funcCall3))
    assertFalse(funcCall1.isEqualTo(funcCall4))
    assertFalse(funcCall1.isEqualTo(funcCall5))

    assertTrue(namedFuncCall1.isEqualTo(namedFuncCall2))
    assertFalse(namedFuncCall1.isEqualTo(namedFuncCall3))
    assertFalse(namedFuncCall1.isEqualTo(namedFuncCall4))
  }

  @Test
  fun testConstructorCallEquality() {
    val superConstrCall1 = superConstrCall(listOf(int(1), string("hello")))
    val superConstrCall2 = superConstrCall(listOf(int(1), string("hello")))
    val superConstrCall3 = superConstrCall(listOf(int(2), string("hello"))) // different args

    val thisConstrCall1 = thisConstrCall(listOf(int(1), string("hello")))
    val thisConstrCall2 = thisConstrCall(listOf(int(1), string("hello")))
    val thisConstrCall3 = thisConstrCall(listOf(int(2), string("hello"))) // different args

    assertTrue(superConstrCall1.isEqualTo(superConstrCall2))
    assertFalse(superConstrCall1.isEqualTo(superConstrCall3))
    assertTrue(thisConstrCall1.isEqualTo(thisConstrCall2))
    assertFalse(thisConstrCall1.isEqualTo(thisConstrCall3))
    assertFalse(superConstrCall1.isEqualTo(thisConstrCall1))
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

    assertTrue(newInstance1.isEqualTo(newInstance2))
    assertFalse(newInstance1.isEqualTo(newInstance3))
    assertFalse(newInstance1.isEqualTo(newInstance4))

    assertTrue(namedNewInstance1.isEqualTo(namedNewInstance2))
    assertFalse(namedNewInstance1.isEqualTo(namedNewInstance3))
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

    assertTrue(whenNode1.isEqualTo(whenNode2))
    assertFalse(whenNode1.isEqualTo(whenNode3))
    assertFalse(whenNode1.isEqualTo(whenNode4))

    assertTrue(switchNode1.isEqualTo(switchNode2))
    assertFalse(switchNode1.isEqualTo(switchNode3))
    assertFalse(whenNode1.isEqualTo(switchNode1)) // when and switch are different
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

    assertTrue(asyncBlock1.isEqualTo(asyncBlock2))
    assertFalse(asyncBlock1.isEqualTo(asyncBlock3))
  }

  @Test
  fun testTruthyVariableDeclarationEquality() {
    val truthyVar1 = truthyVarDecl(type("boolean"), "result", eq(ref("x"), int(0)))
    val truthyVar2 = truthyVarDecl(type("boolean"), "result", eq(ref("x"), int(0)))
    val truthyVar3 = truthyVarDecl(type("boolean"), "different", eq(ref("x"), int(0))) // different name
    val truthyVar4 = truthyVarDecl(type("boolean"), "result", eq(ref("x"), int(1))) // different expression

    assertTrue(truthyVar1.isEqualTo(truthyVar2))
    assertFalse(truthyVar1.isEqualTo(truthyVar3))
    assertFalse(truthyVar1.isEqualTo(truthyVar4))
  }

  @Test
  fun testStatementNodesEquality() {
    // Test ExpressionStatementCstNode
    val exprStmt1 = stmt(fCall("println", listOf(string("hello"))))
    val exprStmt2 = stmt(fCall("println", listOf(string("hello"))))
    val exprStmt3 = stmt(fCall("println", listOf(string("world"))))

    assertTrue(exprStmt1.isEqualTo(exprStmt2))
    assertFalse(exprStmt1.isEqualTo(exprStmt3))

    // Test ReturnCstNode
    val returnStmt1 = returnStmt(int(42))
    val returnStmt2 = returnStmt(int(42))
    val returnStmt3 = returnStmt(int(43))

    assertTrue(returnStmt1.isEqualTo(returnStmt2))
    assertFalse(returnStmt1.isEqualTo(returnStmt3))

    // Test VariableDeclarationCstNode
    val varDecl1 = varDeclStmt(type("int"), "x", int(42))
    val varDecl2 = varDeclStmt(type("int"), "x", int(42))
    val varDecl3 = varDeclStmt(type("int"), "y", int(42)) // different name
    val varDecl4 = varDeclStmt(type("int"), "x", int(43)) // different value
    val varDecl5 = varDeclStmt(type("String"), "x", int(42)) // different type

    assertTrue(varDecl1.isEqualTo(varDecl2))
    assertFalse(varDecl1.isEqualTo(varDecl3))
    assertFalse(varDecl1.isEqualTo(varDecl4))
    assertFalse(varDecl1.isEqualTo(varDecl5))

    // Test MultiVarDeclarationCstNode
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

    assertTrue(multiVarDecl1.isEqualTo(multiVarDecl2))
    assertFalse(multiVarDecl1.isEqualTo(multiVarDecl3))

    // Test IfStatementCstNode
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

    assertTrue(ifStmt1.isEqualTo(ifStmt2))
    assertFalse(ifStmt1.isEqualTo(ifStmt3))

    // Test ForInCstNode
    val forInStmt1 = forInStmt(type("int"), "i", false, array(int(1), int(2), int(3))) {
      stmt(fCall("println", listOf(ref("i"))))
    }

    val forInStmt2 = forInStmt(type("int"), "i", false, array(int(1), int(2), int(3))) {
      stmt(fCall("println", listOf(ref("i"))))
    }

    val forInStmt3 = forInStmt(type("int"), "j", false, array(int(1), int(2), int(3))) { // different var name
      stmt(fCall("println", listOf(ref("j"))))
    }

    assertTrue(forInStmt1.isEqualTo(forInStmt2))
    assertFalse(forInStmt1.isEqualTo(forInStmt3))

    // Test BlockCstNode
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

    assertTrue(block1.isEqualTo(block2))
    assertFalse(block1.isEqualTo(block3))

    // Test BreakCstNode and ContinueCstNode
    val breakStmt1 = breakStmt()
    val breakStmt2 = breakStmt()
    val continueStmt1 = continueStmt()
    val continueStmt2 = continueStmt()

    assertTrue(breakStmt1.isEqualTo(breakStmt2))
    assertTrue(continueStmt1.isEqualTo(continueStmt2))
    assertFalse(breakStmt1.isEqualTo(continueStmt1))

    // Test ThrowCstNode
    val throwStmt1 = throwStmt(new(type("Exception"), listOf(string("error"))))
    val throwStmt2 = throwStmt(new(type("Exception"), listOf(string("error"))))
    val throwStmt3 = throwStmt(new(type("Exception"), listOf(string("different"))))

    assertTrue(throwStmt1.isEqualTo(throwStmt2))
    assertFalse(throwStmt1.isEqualTo(throwStmt3))

    // Test TryCatchCstNode
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

    assertTrue(tryCatchStmt1.isEqualTo(tryCatchStmt2))
    assertFalse(tryCatchStmt1.isEqualTo(tryCatchStmt3))
  }
}