package com.tambapps.marcel.parser.ast

import org.objectweb.asm.MethodVisitor


interface Statement: TokenNode {

  fun write(visitor: MethodVisitor)
}