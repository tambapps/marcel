// DO NOT EDIT
// Generated by JFlex 1.8.2 http://jflex.de/
// source: /Users/nfonkoua/workspace/marcel/marcel-lexer/src/main/java/com/tambapps/marcel/lexer/Marcel.flex

package com.tambapps.marcel.lexer;
import static com.tambapps.marcel.lexer.TokenTypes.*;

/**
  * Marcel lang lexer
  */

// See https://github.com/jflex-de/jflex/issues/222
@SuppressWarnings("FallThrough")
class MarcelJflexer {

  /** This character denotes the end of file. */
  public static final int YYEOF = -1;

  /** Initial size of the lookahead buffer. */
  private static final int ZZ_BUFFERSIZE = 16384;

  // Lexical states.
  public static final int YYINITIAL = 0;

  /**
   * ZZ_LEXSTATE[l] is the state in the DFA for the lexical state l
   * ZZ_LEXSTATE[l+1] is the state in the DFA for the lexical state l
   *                  at the beginning of a line
   * l is of the form l = 2*k, k a non negative integer
   */
  private static final int ZZ_LEXSTATE[] = {
     0, 0
  };

  /**
   * Top-level table for translating characters to character classes
   */
  private static final int [] ZZ_CMAP_TOP = zzUnpackcmap_top();

  private static final String ZZ_CMAP_TOP_PACKED_0 =
    "\1\0\1\u0100\1\u0200\1\u0300\1\u0400\1\u0500\1\u0600\1\u0700"+
    "\1\u0800\1\u0900\1\u0a00\1\u0b00\1\u0c00\1\u0d00\1\u0e00\1\u0f00"+
    "\1\u1000\1\u0100\1\u1100\1\u1200\1\u1300\1\u0100\1\u1400\1\u1500"+
    "\1\u1600\1\u1700\1\u1800\1\u1900\1\u1a00\1\u1b00\1\u0100\1\u1c00"+
    "\1\u1d00\1\u1e00\12\u1f00\1\u2000\1\u2100\1\u2200\1\u1f00\1\u2300"+
    "\1\u2400\2\u1f00\31\u0100\1\u2500\121\u0100\1\u2600\4\u0100\1\u2700"+
    "\1\u0100\1\u2800\1\u2900\1\u2a00\1\u2b00\1\u2c00\1\u2d00\53\u0100"+
    "\1\u2e00\41\u1f00\1\u0100\1\u2f00\1\u3000\1\u0100\1\u3100\1\u3200"+
    "\1\u3300\1\u3400\1\u1f00\1\u3500\1\u3600\1\u3700\1\u3800\1\u0100"+
    "\1\u3900\1\u3a00\1\u3b00\1\u3c00\1\u3d00\1\u3e00\1\u3f00\1\u1f00"+
    "\1\u4000\1\u4100\1\u4200\1\u4300\1\u4400\1\u4500\1\u4600\1\u4700"+
    "\1\u4800\1\u4900\1\u4a00\1\u4b00\1\u1f00\1\u4c00\1\u4d00\1\u4e00"+
    "\1\u1f00\3\u0100\1\u4f00\1\u5000\1\u5100\12\u1f00\4\u0100\1\u5200"+
    "\17\u1f00\2\u0100\1\u5300\41\u1f00\2\u0100\1\u5400\1\u5500\2\u1f00"+
    "\1\u5600\1\u5700\27\u0100\1\u5800\2\u0100\1\u5900\45\u1f00\1\u0100"+
    "\1\u5a00\1\u5b00\11\u1f00\1\u5c00\27\u1f00\1\u5d00\1\u5e00\1\u5f00"+
    "\1\u6000\11\u1f00\1\u6100\1\u6200\5\u1f00\1\u6300\1\u6400\4\u1f00"+
    "\1\u6500\21\u1f00\246\u0100\1\u6600\20\u0100\1\u6700\1\u6800\25\u0100"+
    "\1\u6900\34\u0100\1\u6a00\14\u1f00\2\u0100\1\u6b00\u0e05\u1f00";

  private static int [] zzUnpackcmap_top() {
    int [] result = new int[4352];
    int offset = 0;
    offset = zzUnpackcmap_top(ZZ_CMAP_TOP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackcmap_top(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /**
   * Second-level tables for translating characters to character classes
   */
  private static final int [] ZZ_CMAP_BLOCKS = zzUnpackcmap_blocks();

  private static final String ZZ_CMAP_BLOCKS_PACKED_0 =
    "\12\0\1\1\35\0\1\2\1\3\6\0\1\4\11\5"+
    "\7\0\1\6\1\7\4\6\5\10\1\11\10\10\1\12"+
    "\2\10\1\13\2\10\4\0\1\14\1\15\1\6\1\7"+
    "\4\6\5\10\1\11\10\10\1\12\2\10\1\13\2\10"+
    "\57\0\1\10\12\0\1\10\4\0\1\10\5\0\27\10"+
    "\1\0\37\10\1\0\u01ca\10\4\0\14\10\16\0\5\10"+
    "\7\0\1\10\1\0\1\10\201\0\5\10\1\0\2\10"+
    "\2\0\4\10\1\0\1\10\6\0\1\10\1\0\3\10"+
    "\1\0\1\10\1\0\24\10\1\0\123\10\1\0\213\10"+
    "\10\0\246\10\1\0\46\10\2\0\1\10\6\0\51\10"+
    "\107\0\33\10\4\0\4\10\55\0\53\10\25\0\12\16"+
    "\4\0\2\10\1\0\143\10\1\0\1\10\17\0\2\10"+
    "\7\0\2\10\12\16\3\10\2\0\1\10\20\0\1\10"+
    "\1\0\36\10\35\0\131\10\13\0\1\10\16\0\12\16"+
    "\41\10\11\0\2\10\4\0\1\10\5\0\26\10\4\0"+
    "\1\10\11\0\1\10\3\0\1\10\27\0\31\10\7\0"+
    "\13\10\65\0\25\10\1\0\10\10\106\0\66\10\3\0"+
    "\1\10\22\0\1\10\7\0\12\10\4\0\12\16\1\0"+
    "\20\10\4\0\10\10\2\0\2\10\2\0\26\10\1\0"+
    "\7\10\1\0\1\10\3\0\4\10\3\0\1\10\20\0"+
    "\1\10\15\0\2\10\1\0\3\10\4\0\12\16\2\10"+
    "\12\0\1\10\10\0\6\10\4\0\2\10\2\0\26\10"+
    "\1\0\7\10\1\0\2\10\1\0\2\10\1\0\2\10"+
    "\37\0\4\10\1\0\1\10\7\0\12\16\2\0\3\10"+
    "\20\0\11\10\1\0\3\10\1\0\26\10\1\0\7\10"+
    "\1\0\2\10\1\0\5\10\3\0\1\10\22\0\1\10"+
    "\17\0\2\10\4\0\12\16\11\0\1\10\13\0\10\10"+
    "\2\0\2\10\2\0\26\10\1\0\7\10\1\0\2\10"+
    "\1\0\5\10\3\0\1\10\36\0\2\10\1\0\3\10"+
    "\4\0\12\16\1\0\1\10\21\0\1\10\1\0\6\10"+
    "\3\0\3\10\1\0\4\10\3\0\2\10\1\0\1\10"+
    "\1\0\2\10\3\0\2\10\3\0\3\10\3\0\14\10"+
    "\26\0\1\10\25\0\12\16\25\0\10\10\1\0\3\10"+
    "\1\0\27\10\1\0\20\10\3\0\1\10\32\0\3\10"+
    "\5\0\2\10\4\0\12\16\20\0\1\10\4\0\10\10"+
    "\1\0\3\10\1\0\27\10\1\0\12\10\1\0\5\10"+
    "\3\0\1\10\40\0\1\10\1\0\2\10\4\0\12\16"+
    "\1\0\2\10\22\0\10\10\1\0\3\10\1\0\51\10"+
    "\2\0\1\10\20\0\1\10\5\0\3\10\10\0\3\10"+
    "\4\0\12\16\12\0\6\10\5\0\22\10\3\0\30\10"+
    "\1\0\11\10\1\0\1\10\2\0\7\10\37\0\12\16"+
    "\21\0\60\10\1\0\2\10\14\0\7\10\11\0\12\16"+
    "\47\0\2\10\1\0\1\10\1\0\5\10\1\0\30\10"+
    "\1\0\1\10\1\0\12\10\1\0\2\10\11\0\1\10"+
    "\2\0\5\10\1\0\1\10\11\0\12\16\2\0\4\10"+
    "\40\0\1\10\37\0\12\16\26\0\10\10\1\0\44\10"+
    "\33\0\5\10\163\0\53\10\24\0\1\10\12\16\6\0"+
    "\6\10\4\0\4\10\3\0\1\10\3\0\2\10\7\0"+
    "\3\10\4\0\15\10\14\0\1\10\1\0\12\16\6\0"+
    "\46\10\1\0\1\10\5\0\1\10\2\0\53\10\1\0"+
    "\115\10\1\0\4\10\2\0\7\10\1\0\1\10\1\0"+
    "\4\10\2\0\51\10\1\0\4\10\2\0\41\10\1\0"+
    "\4\10\2\0\7\10\1\0\1\10\1\0\4\10\2\0"+
    "\17\10\1\0\71\10\1\0\4\10\2\0\103\10\45\0"+
    "\20\10\20\0\126\10\2\0\6\10\3\0\u016c\10\2\0"+
    "\21\10\1\0\32\10\5\0\113\10\6\0\10\10\7\0"+
    "\15\10\1\0\4\10\16\0\22\10\16\0\22\10\16\0"+
    "\15\10\1\0\3\10\17\0\64\10\43\0\1\10\4\0"+
    "\1\10\3\0\12\16\46\0\12\16\6\0\131\10\7\0"+
    "\5\10\2\0\42\10\1\0\1\10\5\0\106\10\12\0"+
    "\37\10\47\0\12\16\36\10\2\0\5\10\13\0\54\10"+
    "\4\0\32\10\6\0\12\16\46\0\27\10\11\0\65\10"+
    "\53\0\12\16\6\0\12\16\15\0\1\10\135\0\57\10"+
    "\21\0\7\10\4\0\12\16\51\0\36\10\15\0\2\10"+
    "\12\16\54\10\32\0\44\10\34\0\12\16\3\0\3\10"+
    "\12\16\44\10\2\0\11\10\7\0\53\10\2\0\3\10"+
    "\51\0\4\10\1\0\6\10\1\0\2\10\3\0\1\10"+
    "\5\0\300\10\100\0\26\10\2\0\6\10\2\0\46\10"+
    "\2\0\6\10\2\0\10\10\1\0\1\10\1\0\1\10"+
    "\1\0\1\10\1\0\37\10\2\0\65\10\1\0\7\10"+
    "\1\0\1\10\3\0\3\10\1\0\7\10\3\0\4\10"+
    "\2\0\6\10\4\0\15\10\5\0\3\10\1\0\7\10"+
    "\164\0\1\10\15\0\1\10\20\0\15\10\145\0\1\10"+
    "\4\0\1\10\2\0\12\10\1\0\1\10\3\0\5\10"+
    "\6\0\1\10\1\0\1\10\1\0\1\10\1\0\4\10"+
    "\1\0\13\10\2\0\4\10\5\0\5\10\4\0\1\10"+
    "\64\0\2\10\u017b\0\57\10\1\0\57\10\1\0\205\10"+
    "\6\0\4\10\3\0\2\10\14\0\46\10\1\0\1\10"+
    "\5\0\1\10\2\0\70\10\7\0\1\10\20\0\27\10"+
    "\11\0\7\10\1\0\7\10\1\0\7\10\1\0\7\10"+
    "\1\0\7\10\1\0\7\10\1\0\7\10\1\0\7\10"+
    "\120\0\1\10\325\0\2\10\52\0\5\10\5\0\2\10"+
    "\4\0\126\10\6\0\3\10\1\0\132\10\1\0\4\10"+
    "\5\0\53\10\1\0\136\10\21\0\33\10\65\0\306\10"+
    "\112\0\360\10\20\0\215\10\103\0\56\10\2\0\15\10"+
    "\3\0\20\10\12\16\2\10\24\0\57\10\20\0\37\10"+
    "\2\0\106\10\61\0\11\10\2\0\147\10\2\0\65\10"+
    "\2\0\5\10\60\0\13\10\1\0\3\10\1\0\4\10"+
    "\1\0\27\10\35\0\64\10\16\0\62\10\34\0\12\16"+
    "\30\0\6\10\3\0\1\10\1\0\2\10\1\0\12\16"+
    "\34\10\12\0\27\10\31\0\35\10\7\0\57\10\34\0"+
    "\1\10\12\16\6\0\5\10\1\0\12\10\12\16\5\10"+
    "\1\0\51\10\27\0\3\10\1\0\10\10\4\0\12\16"+
    "\6\0\27\10\3\0\1\10\3\0\62\10\1\0\1\10"+
    "\3\0\2\10\2\0\5\10\2\0\1\10\1\0\1\10"+
    "\30\0\3\10\2\0\13\10\7\0\3\10\14\0\6\10"+
    "\2\0\6\10\2\0\6\10\11\0\7\10\1\0\7\10"+
    "\1\0\53\10\1\0\14\10\10\0\163\10\15\0\12\16"+
    "\6\0\244\10\14\0\27\10\4\0\61\10\4\0\156\10"+
    "\2\0\152\10\46\0\7\10\14\0\5\10\5\0\1\10"+
    "\1\0\12\10\1\0\15\10\1\0\5\10\1\0\1\10"+
    "\1\0\2\10\1\0\2\10\1\0\154\10\41\0\153\10"+
    "\22\0\100\10\2\0\66\10\50\0\14\10\164\0\5\10"+
    "\1\0\207\10\23\0\12\16\7\0\32\10\6\0\32\10"+
    "\13\0\131\10\3\0\6\10\2\0\6\10\2\0\6\10"+
    "\2\0\3\10\43\0\14\10\1\0\32\10\1\0\23\10"+
    "\1\0\2\10\1\0\17\10\2\0\16\10\42\0\173\10"+
    "\205\0\35\10\3\0\61\10\57\0\40\10\15\0\24\10"+
    "\1\0\10\10\6\0\46\10\12\0\36\10\2\0\44\10"+
    "\4\0\10\10\60\0\236\10\2\0\12\16\6\0\44\10"+
    "\4\0\44\10\4\0\50\10\10\0\64\10\234\0\67\10"+
    "\11\0\26\10\12\0\10\10\230\0\6\10\2\0\1\10"+
    "\1\0\54\10\1\0\2\10\3\0\1\10\2\0\27\10"+
    "\12\0\27\10\11\0\37\10\101\0\23\10\1\0\2\10"+
    "\12\0\26\10\12\0\32\10\106\0\70\10\6\0\2\10"+
    "\100\0\1\10\17\0\4\10\1\0\3\10\1\0\35\10"+
    "\52\0\35\10\3\0\35\10\43\0\10\10\1\0\34\10"+
    "\33\0\66\10\12\0\26\10\12\0\23\10\15\0\22\10"+
    "\156\0\111\10\67\0\63\10\15\0\63\10\15\0\44\10"+
    "\14\0\12\16\306\0\35\10\12\0\1\10\10\0\26\10"+
    "\232\0\27\10\14\0\65\10\56\0\12\16\23\0\55\10"+
    "\40\0\31\10\7\0\12\16\11\0\44\10\17\0\12\16"+
    "\4\0\1\10\13\0\43\10\3\0\1\10\14\0\60\10"+
    "\16\0\4\10\13\0\12\16\1\10\1\0\1\10\43\0"+
    "\22\10\1\0\31\10\124\0\7\10\1\0\1\10\1\0"+
    "\4\10\1\0\17\10\1\0\12\10\7\0\57\10\21\0"+
    "\12\16\13\0\10\10\2\0\2\10\2\0\26\10\1\0"+
    "\7\10\1\0\2\10\1\0\5\10\3\0\1\10\22\0"+
    "\1\10\14\0\5\10\236\0\65\10\22\0\4\10\5\0"+
    "\12\16\5\0\1\10\40\0\60\10\24\0\2\10\1\0"+
    "\1\10\10\0\12\16\246\0\57\10\51\0\4\10\44\0"+
    "\60\10\24\0\1\10\13\0\12\16\46\0\53\10\15\0"+
    "\1\10\7\0\12\16\66\0\33\10\25\0\12\16\306\0"+
    "\54\10\164\0\100\10\12\16\25\0\1\10\240\0\10\10"+
    "\2\0\47\10\20\0\1\10\1\0\1\10\34\0\1\10"+
    "\12\0\50\10\7\0\1\10\25\0\1\10\13\0\56\10"+
    "\23\0\1\10\42\0\71\10\7\0\11\10\1\0\45\10"+
    "\21\0\1\10\17\0\12\16\30\0\36\10\160\0\7\10"+
    "\1\0\2\10\1\0\46\10\25\0\1\10\11\0\12\16"+
    "\6\0\6\10\1\0\2\10\1\0\40\10\16\0\1\10"+
    "\7\0\12\16\u0136\0\23\10\15\0\232\10\346\0\304\10"+
    "\274\0\57\10\321\0\107\10\271\0\71\10\7\0\37\10"+
    "\1\0\12\16\146\0\36\10\22\0\60\10\20\0\4\10"+
    "\14\0\12\16\11\0\25\10\5\0\23\10\260\0\100\10"+
    "\200\0\113\10\5\0\1\10\102\0\15\10\100\0\2\10"+
    "\1\0\1\10\34\0\370\10\10\0\363\10\15\0\37\10"+
    "\61\0\3\10\21\0\4\10\10\0\u018c\10\4\0\153\10"+
    "\5\0\15\10\3\0\11\10\7\0\12\10\146\0\125\10"+
    "\1\0\107\10\1\0\2\10\2\0\1\10\2\0\2\10"+
    "\2\0\4\10\1\0\14\10\1\0\1\10\1\0\7\10"+
    "\1\0\101\10\1\0\4\10\2\0\10\10\1\0\7\10"+
    "\1\0\34\10\1\0\4\10\1\0\5\10\1\0\1\10"+
    "\3\0\7\10\1\0\u0154\10\2\0\31\10\1\0\31\10"+
    "\1\0\37\10\1\0\31\10\1\0\37\10\1\0\31\10"+
    "\1\0\37\10\1\0\31\10\1\0\37\10\1\0\31\10"+
    "\1\0\10\10\2\0\62\16\55\10\12\0\7\10\2\0"+
    "\12\16\4\0\1\10\u0171\0\54\10\4\0\12\16\6\0"+
    "\305\10\73\0\104\10\7\0\1\10\4\0\12\16\246\0"+
    "\4\10\1\0\33\10\1\0\2\10\1\0\1\10\2\0"+
    "\1\10\1\0\12\10\1\0\4\10\1\0\1\10\1\0"+
    "\1\10\6\0\1\10\4\0\1\10\1\0\1\10\1\0"+
    "\1\10\1\0\3\10\1\0\2\10\1\0\1\10\2\0"+
    "\1\10\1\0\1\10\1\0\1\10\1\0\1\10\1\0"+
    "\1\10\1\0\2\10\1\0\1\10\2\0\4\10\1\0"+
    "\7\10\1\0\4\10\1\0\4\10\1\0\1\10\1\0"+
    "\12\10\1\0\21\10\5\0\3\10\1\0\5\10\1\0"+
    "\21\10\104\0\327\10\51\0\65\10\13\0\336\10\2\0"+
    "\u0182\10\16\0\u0131\10\37\0\36\10\342\0";

  private static int [] zzUnpackcmap_blocks() {
    int [] result = new int[27648];
    int offset = 0;
    offset = zzUnpackcmap_blocks(ZZ_CMAP_BLOCKS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackcmap_blocks(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  /**
   * Translates DFA states to action switch labels.
   */
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\1\0\1\1\1\2\2\3\1\4\1\0\3\3\1\0"+
    "\1\4";

  private static int [] zzUnpackAction() {
    int [] result = new int[12];
    int offset = 0;
    offset = zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAction(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /**
   * Translates a state to a row index in the transition table
   */
  private static final int [] ZZ_ROWMAP = zzUnpackRowMap();

  private static final String ZZ_ROWMAP_PACKED_0 =
    "\0\0\0\17\0\17\0\36\0\55\0\74\0\113\0\17"+
    "\0\132\0\151\0\170\0\17";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[12];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int [] result) {
    int i = 0;  /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int high = packed.charAt(i++) << 16;
      result[j++] = high | packed.charAt(i++);
    }
    return j;
  }

  /**
   * The transition table of the DFA
   */
  private static final int [] ZZ_TRANS = zzUnpackTrans();

  private static final String ZZ_TRANS_PACKED_0 =
    "\2\0\1\2\1\3\1\4\1\5\7\6\1\7\27\0"+
    "\1\5\1\0\1\10\1\11\1\12\7\0\2\5\3\0"+
    "\1\10\1\11\1\0\1\5\6\0\11\6\1\0\1\6"+
    "\1\13\1\0\13\13\1\0\1\13\11\0\1\10\11\0"+
    "\4\12\1\0\1\10\1\11\1\0\1\12\2\0\1\13"+
    "\1\0\13\13\1\14\1\13";

  private static int [] zzUnpackTrans() {
    int [] result = new int[135];
    int offset = 0;
    offset = zzUnpackTrans(ZZ_TRANS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackTrans(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /** Error code for "Unknown internal scanner error". */
  private static final int ZZ_UNKNOWN_ERROR = 0;
  /** Error code for "could not match input". */
  private static final int ZZ_NO_MATCH = 1;
  /** Error code for "pushback value was too large". */
  private static final int ZZ_PUSHBACK_2BIG = 2;

  /**
   * Error messages for {@link #ZZ_UNKNOWN_ERROR}, {@link #ZZ_NO_MATCH}, and
   * {@link #ZZ_PUSHBACK_2BIG} respectively.
   */
  private static final String ZZ_ERROR_MSG[] = {
    "Unknown internal scanner error",
    "Error: could not match input",
    "Error: pushback value was too large"
  };

  /**
   * ZZ_ATTRIBUTE[aState] contains the attributes of state {@code aState}
   */
  private static final int [] ZZ_ATTRIBUTE = zzUnpackAttribute();

  private static final String ZZ_ATTRIBUTE_PACKED_0 =
    "\1\0\2\11\3\1\1\0\1\11\2\1\1\0\1\11";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[12];
    int offset = 0;
    offset = zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAttribute(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  /** Input device. */
  private java.io.Reader zzReader;

  /** Current state of the DFA. */
  private int zzState;

  /** Current lexical state. */
  private int zzLexicalState = YYINITIAL;

  /**
   * This buffer contains the current text to be matched and is the source of the {@link #yytext()}
   * string.
   */
  private char zzBuffer[] = new char[ZZ_BUFFERSIZE];

  /** Text position at the last accepting state. */
  private int zzMarkedPos;

  /** Current text position in the buffer. */
  private int zzCurrentPos;

  /** Marks the beginning of the {@link #yytext()} string in the buffer. */
  private int zzStartRead;

  /** Marks the last character in the buffer, that has been read from input. */
  private int zzEndRead;

  /**
   * Whether the scanner is at the end of file.
   * @see #yyatEOF
   */
  private boolean zzAtEOF;

  /**
   * The number of occupied positions in {@link #zzBuffer} beyond {@link #zzEndRead}.
   *
   * <p>When a lead/high surrogate has been read from the input stream into the final
   * {@link #zzBuffer} position, this will have a value of 1; otherwise, it will have a value of 0.
   */
  private int zzFinalHighSurrogate = 0;

  /** Number of newlines encountered up to the start of the matched text. */
  private int yyline;

  /** Number of characters from the last newline up to the start of the matched text. */
  private int yycolumn;

  /** Number of characters up to the start of the matched text. */
  @SuppressWarnings("unused")
  private long yychar;

  /** Whether the scanner is currently at the beginning of a line. */
  @SuppressWarnings("unused")
  private boolean zzAtBOL = true;

  /** Whether the user-EOF-code has already been executed. */
  private boolean zzEOFDone;

  /* user code: */
  // tokens for which we need to save current buffer
  private LexToken valueToken(int tokenType) {
    return new LexToken(tokenType, new String(zzBuffer, zzCurrentPos, zzMarkedPos - zzCurrentPos));
  }
  private LexToken token(int tokenType) {
    return new LexToken(tokenType, null);
  }



  /**
   * Creates a new scanner
   *
   * @param   in  the java.io.Reader to read input from.
   */
  MarcelJflexer(java.io.Reader in) {
    this.zzReader = in;
  }

  /**
   * Translates raw input code points to DFA table row
   */
  private static int zzCMap(int input) {
    int offset = input & 255;
    return offset == input ? ZZ_CMAP_BLOCKS[offset] : ZZ_CMAP_BLOCKS[ZZ_CMAP_TOP[input >> 8] | offset];
  }

  /**
   * Refills the input buffer.
   *
   * @return {@code false} iff there was new input.
   * @exception java.io.IOException  if any I/O-Error occurs
   */
  private boolean zzRefill() throws java.io.IOException {

    /* first: make room (if you can) */
    if (zzStartRead > 0) {
      zzEndRead += zzFinalHighSurrogate;
      zzFinalHighSurrogate = 0;
      System.arraycopy(zzBuffer, zzStartRead,
                       zzBuffer, 0,
                       zzEndRead - zzStartRead);

      /* translate stored positions */
      zzEndRead -= zzStartRead;
      zzCurrentPos -= zzStartRead;
      zzMarkedPos -= zzStartRead;
      zzStartRead = 0;
    }

    /* is the buffer big enough? */
    if (zzCurrentPos >= zzBuffer.length - zzFinalHighSurrogate) {
      /* if not: blow it up */
      char newBuffer[] = new char[zzBuffer.length * 2];
      System.arraycopy(zzBuffer, 0, newBuffer, 0, zzBuffer.length);
      zzBuffer = newBuffer;
      zzEndRead += zzFinalHighSurrogate;
      zzFinalHighSurrogate = 0;
    }

    /* fill the buffer with new input */
    int requested = zzBuffer.length - zzEndRead;
    int numRead = zzReader.read(zzBuffer, zzEndRead, requested);

    /* not supposed to occur according to specification of java.io.Reader */
    if (numRead == 0) {
      throw new java.io.IOException(
          "Reader returned 0 characters. See JFlex examples/zero-reader for a workaround.");
    }
    if (numRead > 0) {
      zzEndRead += numRead;
      if (Character.isHighSurrogate(zzBuffer[zzEndRead - 1])) {
        if (numRead == requested) { // We requested too few chars to encode a full Unicode character
          --zzEndRead;
          zzFinalHighSurrogate = 1;
        } else {                    // There is room in the buffer for at least one more char
          int c = zzReader.read();  // Expecting to read a paired low surrogate char
          if (c == -1) {
            return true;
          } else {
            zzBuffer[zzEndRead++] = (char)c;
          }
        }
      }
      /* potentially more input available */
      return false;
    }

    /* numRead < 0 ==> end of stream */
    return true;
  }


  /**
   * Closes the input reader.
   *
   * @throws java.io.IOException if the reader could not be closed.
   */
  public final void yyclose() throws java.io.IOException {
    zzAtEOF = true; // indicate end of file
    zzEndRead = zzStartRead; // invalidate buffer

    if (zzReader != null) {
      zzReader.close();
    }
  }


  /**
   * Resets the scanner to read from a new input stream.
   *
   * <p>Does not close the old reader.
   *
   * <p>All internal variables are reset, the old input stream <b>cannot</b> be reused (internal
   * buffer is discarded and lost). Lexical state is set to {@code ZZ_INITIAL}.
   *
   * <p>Internal scan buffer is resized down to its initial length, if it has grown.
   *
   * @param reader The new input stream.
   */
  public final void yyreset(java.io.Reader reader) {
    zzReader = reader;
    zzEOFDone = false;
    yyResetPosition();
    zzLexicalState = YYINITIAL;
    if (zzBuffer.length > ZZ_BUFFERSIZE) {
      zzBuffer = new char[ZZ_BUFFERSIZE];
    }
  }

  /**
   * Resets the input position.
   */
  private final void yyResetPosition() {
      zzAtBOL  = true;
      zzAtEOF  = false;
      zzCurrentPos = 0;
      zzMarkedPos = 0;
      zzStartRead = 0;
      zzEndRead = 0;
      zzFinalHighSurrogate = 0;
      yyline = 0;
      yycolumn = 0;
      yychar = 0L;
  }


  /**
   * Returns whether the scanner has reached the end of the reader it reads from.
   *
   * @return whether the scanner has reached EOF.
   */
  public final boolean yyatEOF() {
    return zzAtEOF;
  }


  /**
   * Returns the current lexical state.
   *
   * @return the current lexical state.
   */
  public final int yystate() {
    return zzLexicalState;
  }


  /**
   * Enters a new lexical state.
   *
   * @param newState the new lexical state
   */
  public final void yybegin(int newState) {
    zzLexicalState = newState;
  }


  /**
   * Returns the text matched by the current regular expression.
   *
   * @return the matched text.
   */
  public final String yytext() {
    return new String(zzBuffer, zzStartRead, zzMarkedPos-zzStartRead);
  }


  /**
   * Returns the character at the given position from the matched text.
   *
   * <p>It is equivalent to {@code yytext().charAt(pos)}, but faster.
   *
   * @param position the position of the character to fetch. A value from 0 to {@code yylength()-1}.
   *
   * @return the character at {@code position}.
   */
  public final char yycharat(int position) {
    return zzBuffer[zzStartRead + position];
  }


  /**
   * How many characters were matched.
   *
   * @return the length of the matched text region.
   */
  public final int yylength() {
    return zzMarkedPos-zzStartRead;
  }


  /**
   * Reports an error that occurred while scanning.
   *
   * <p>In a well-formed scanner (no or only correct usage of {@code yypushback(int)} and a
   * match-all fallback rule) this method will only be called with things that
   * "Can't Possibly Happen".
   *
   * <p>If this method is called, something is seriously wrong (e.g. a JFlex bug producing a faulty
   * scanner etc.).
   *
   * <p>Usual syntax/scanner level error handling should be done in error fallback rules.
   *
   * @param errorCode the code of the error message to display.
   */
  private static void zzScanError(int errorCode) throws MarcelLexerException {
    String message;
    try {
      message = ZZ_ERROR_MSG[errorCode];
    } catch (ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
    }

    throw new MarcelLexerException(message);
  }


  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * <p>They will be read again by then next call of the scanning method.
   *
   * @param number the number of characters to be read again. This number must not be greater than
   *     {@link #yylength()}.
   */
  public void yypushback(int number)  throws MarcelLexerException {
    if ( number > yylength() )
      zzScanError(ZZ_PUSHBACK_2BIG);

    zzMarkedPos -= number;
  }


  /**
   * Contains user EOF-code, which will be executed exactly once,
   * when the end of file is reached
   */
  private void zzDoEOF() {
    if (!zzEOFDone) {
      zzEOFDone = true;
    
  // end of file
    }
  }




  /**
   * Resumes scanning until the next regular expression is matched, the end of input is encountered
   * or an I/O-Error occurs.
   *
   * @return the next token.
   * @exception java.io.IOException if any I/O-Error occurs.
   */
  public LexToken nextToken() throws java.io.IOException, MarcelLexerException {
    int zzInput;
    int zzAction;

    // cached fields:
    int zzCurrentPosL;
    int zzMarkedPosL;
    int zzEndReadL = zzEndRead;
    char[] zzBufferL = zzBuffer;

    int [] zzTransL = ZZ_TRANS;
    int [] zzRowMapL = ZZ_ROWMAP;
    int [] zzAttrL = ZZ_ATTRIBUTE;

    while (true) {
      zzMarkedPosL = zzMarkedPos;

      boolean zzR = false;
      int zzCh;
      int zzCharCount;
      for (zzCurrentPosL = zzStartRead  ;
           zzCurrentPosL < zzMarkedPosL ;
           zzCurrentPosL += zzCharCount ) {
        zzCh = Character.codePointAt(zzBufferL, zzCurrentPosL, zzMarkedPosL);
        zzCharCount = Character.charCount(zzCh);
        switch (zzCh) {
        case '\u000B':  // fall through
        case '\u000C':  // fall through
        case '\u0085':  // fall through
        case '\u2028':  // fall through
        case '\u2029':
          yyline++;
          yycolumn = 0;
          zzR = false;
          break;
        case '\r':
          yyline++;
          yycolumn = 0;
          zzR = true;
          break;
        case '\n':
          if (zzR)
            zzR = false;
          else {
            yyline++;
            yycolumn = 0;
          }
          break;
        default:
          zzR = false;
          yycolumn += zzCharCount;
        }
      }

      if (zzR) {
        // peek one character ahead if it is
        // (if we have counted one line too much)
        boolean zzPeek;
        if (zzMarkedPosL < zzEndReadL)
          zzPeek = zzBufferL[zzMarkedPosL] == '\n';
        else if (zzAtEOF)
          zzPeek = false;
        else {
          boolean eof = zzRefill();
          zzEndReadL = zzEndRead;
          zzMarkedPosL = zzMarkedPos;
          zzBufferL = zzBuffer;
          if (eof)
            zzPeek = false;
          else
            zzPeek = zzBufferL[zzMarkedPosL] == '\n';
        }
        if (zzPeek) yyline--;
      }
      zzAction = -1;

      zzCurrentPosL = zzCurrentPos = zzStartRead = zzMarkedPosL;

      zzState = ZZ_LEXSTATE[zzLexicalState];

      // set up zzAction for empty match case:
      int zzAttributes = zzAttrL[zzState];
      if ( (zzAttributes & 1) == 1 ) {
        zzAction = zzState;
      }


      zzForAction: {
        while (true) {

          if (zzCurrentPosL < zzEndReadL) {
            zzInput = Character.codePointAt(zzBufferL, zzCurrentPosL, zzEndReadL);
            zzCurrentPosL += Character.charCount(zzInput);
          }
          else if (zzAtEOF) {
            zzInput = YYEOF;
            break zzForAction;
          }
          else {
            // store back cached positions
            zzCurrentPos  = zzCurrentPosL;
            zzMarkedPos   = zzMarkedPosL;
            boolean eof = zzRefill();
            // get translated positions and possibly new buffer
            zzCurrentPosL  = zzCurrentPos;
            zzMarkedPosL   = zzMarkedPos;
            zzBufferL      = zzBuffer;
            zzEndReadL     = zzEndRead;
            if (eof) {
              zzInput = YYEOF;
              break zzForAction;
            }
            else {
              zzInput = Character.codePointAt(zzBufferL, zzCurrentPosL, zzEndReadL);
              zzCurrentPosL += Character.charCount(zzInput);
            }
          }
          int zzNext = zzTransL[ zzRowMapL[zzState] + zzCMap(zzInput) ];
          if (zzNext == -1) break zzForAction;
          zzState = zzNext;

          zzAttributes = zzAttrL[zzState];
          if ( (zzAttributes & 1) == 1 ) {
            zzAction = zzState;
            zzMarkedPosL = zzCurrentPosL;
            if ( (zzAttributes & 8) == 8 ) break zzForAction;
          }

        }
      }

      // store back cached position
      zzMarkedPos = zzMarkedPosL;

      if (zzInput == YYEOF && zzStartRead == zzCurrentPos) {
        zzAtEOF = true;
            zzDoEOF();
        return null;
      }
      else {
        switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction]) {
          case 1:
            { return token(LPAR);
            }
            // fall through
          case 5: break;
          case 2:
            { return token(RPAR);
            }
            // fall through
          case 6: break;
          case 3:
            { return valueToken(INTEGER);
            }
            // fall through
          case 7: break;
          case 4:
            { return valueToken(IDENTIFIER);
            }
            // fall through
          case 8: break;
          default:
            zzScanError(ZZ_NO_MATCH);
        }
      }
    }
  }


}
