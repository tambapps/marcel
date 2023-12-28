
// register code highlight for marcellang
hljs.registerLanguage("marcel", function () {
    "use strict";
    const e = ["int",
            "long",
            "short",
            "float",
            "double",
            "bool",
            "byte",
            "void",
            "char",
            "fun",
            "return",
            "true",
            "false",
            "new",
            "import",
            "as",
            "inline",
            "static",
            "for",
            "in",
            "do",
            "while",
            "if",
            "else",
            "null",
            "break",
            "continue",
            "def",
            "class",
            "extension",
            "package",
            "extends",
            "implements",
            "final",
            "switch",
            "when",
            "this",
            "super",
            "dumbbell",
            "try",
            "catch",
            "finally",
            "instanceof",
            "throw",
            "throws",
            "constructor",
            "public",
            "protected",
            "internal",
            "private"],
        n = ["true", "false", "null"],
        // common functions
        a = [].concat(["map", "find", "findAll", "filter", "any", "none", "all"],
            ["this", "super"],
            // common types
            ["Collection", "List", "Set", "Integer", "Float", "Long", "Double", "Character", "Byte", "Boolean", "Map", "DynamicObject", "String", "Object", "BufferedReader"],
            // Exceptions
            ["RuntimeException", "IllegalArgumentException", "IOException", "Exception", "NullPointerException"]);
    return function (r) {
        var t = {
            $pattern: "[A-Za-z$_][0-9A-Za-z$_]*",
            keyword: e.concat(["class", "enum", "interface", "public", "private", "protected", "implements"]).join(" "),
            literal: n.join(" "),
            built_in: a.concat(["int","long","float","char","bool","double", "void", "class"]).join(" ")
        }, s = {className: "meta", begin: "@[A-Za-z$_][0-9A-Za-z$_]*"}, i = {
            className: "number",
            variants: [{begin: "\\b(0[bB][01]+)n?"}, {begin: "\\b(0[oO][0-7]+)n?"}, {begin: r.C_NUMBER_RE + "n?"}],
            relevance: 0
        }, o = {className: "subst", begin: "\\$\\{", end: "\\}", keywords: t, contains: []}, c = {
            begin: "html`",
            end: "",
            starts: {end: "`", returnEnd: !1, contains: [r.BACKSLASH_ESCAPE, o], subLanguage: "xml"}
        }, l = {
            begin: "css`",
            end: "",
            starts: {end: "`", returnEnd: !1, contains: [r.BACKSLASH_ESCAPE, o], subLanguage: "css"}
        }, E = {className: "string", begin: "`", end: "`", contains: [r.BACKSLASH_ESCAPE, o]};
        o.contains = [r.APOS_STRING_MODE, r.QUOTE_STRING_MODE, c, l, E, i, r.REGEXP_MODE];
        var d = {
            begin: "\\(",
            end: /\)/,
            keywords: t,
            contains: ["this", r.QUOTE_STRING_MODE, r.APOS_STRING_MODE, r.NUMBER_MODE]
        }, u = {
            className: "params",
            begin: /\(/,
            end: /\)/,
            excludeBegin: !0,
            excludeEnd: !0,
            keywords: t,
            contains: [r.C_LINE_COMMENT_MODE, r.C_BLOCK_COMMENT_MODE, s, d]
        };
        return {
            name: "Marcel",
            aliases: ["mcl"],
            keywords: t,
            contains: [r.SHEBANG(), {
                className: "meta",
                begin: /^\s*['"]use strict['"]/
            }, r.APOS_STRING_MODE, r.QUOTE_STRING_MODE, c, l, E, r.C_LINE_COMMENT_MODE, r.C_BLOCK_COMMENT_MODE, i, {
                begin: "(" + r.RE_STARTERS_RE + "|\\b(case|return|throw)\\b)\\s*",
                keywords: "return throw case",
                contains: [r.C_LINE_COMMENT_MODE, r.C_BLOCK_COMMENT_MODE, r.REGEXP_MODE, {
                    className: "function",
                    begin: "(\\([^(]*(\\([^(]*(\\([^(]*\\))?\\))?\\)|" + r.UNDERSCORE_IDENT_RE + ")\\s*=>",
                    returnBegin: !0,
                    end: "\\s*=>",
                    contains: [{
                        className: "params",
                        variants: [{begin: r.UNDERSCORE_IDENT_RE}, {
                            className: null,
                            begin: /\(\s*\)/,
                            skip: !0
                        }, {
                            begin: /\(/,
                            end: /\)/,
                            excludeBegin: !0,
                            excludeEnd: !0,
                            keywords: t,
                            contains: d.contains
                        }]
                    }]
                }],
                relevance: 0
            }, {
                className: "function",
                beginKeywords: "fun",
                end: /[\{;]/,
                excludeEnd: !0,
                keywords: t,
                contains: ["this", r.inherit(r.TITLE_MODE, {begin: "[A-Za-z$_][0-9A-Za-z$_]*"}), u],
                illegal: /%/,
                relevance: 0
            }, {beginKeywords: "constructor", end: /[\{;]/, excludeEnd: !0, contains: ["self", u]}, {
                begin: /module\./,
                keywords: {built_in: "module"},
                relevance: 0
            }, {beginKeywords: "module", end: /\{/, excludeEnd: !0}, {
                beginKeywords: "interface",
                end: /\{/,
                excludeEnd: !0,
                keywords: "interface extends"
            }, {begin: /\$[(.]/}, {begin: "\\." + r.IDENT_RE, relevance: 0}, s, d]
        }
    }
}())