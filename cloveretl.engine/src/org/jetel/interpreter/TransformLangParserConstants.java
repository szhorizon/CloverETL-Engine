/* Generated By:JJTree&JavaCC: Do not edit this line. TransformLangParserConstants.java */
package org.jetel.interpreter;

public interface TransformLangParserConstants {

  int EOF = 0;
  int SINGLE_LINE_COMMENT = 9;
  int INTEGER_LITERAL = 10;
  int DIGIT = 11;
  int LETTER = 12;
  int UNDERSCORE = 13;
  int DECIMAL_LITERAL = 14;
  int HEX_LITERAL = 15;
  int OCTAL_LITERAL = 16;
  int FLOATING_POINT_LITERAL = 17;
  int EXPONENT = 18;
  int STRING_LITERAL = 19;
  int DQUOTED_STRING = 20;
  int UNTERMINATED_STRING_LITERAL = 21;
  int UNTERMINATED_DQUOTED_STRING = 22;
  int BOOLEAN_LITERAL = 23;
  int TRUE = 24;
  int FALSE = 25;
  int DATE_LITERAL = 26;
  int DATETIME_LITERAL = 27;
  int SEMICOLON = 28;
  int BLOCK_START = 29;
  int BLOCK_END = 30;
  int NULL_LITERAL = 31;
  int STRING_PLAIN_LITERAL = 32;
  int MAPPING = 33;
  int OR = 34;
  int AND = 35;
  int NOT = 36;
  int EQUAL = 37;
  int NON_EQUAL = 38;
  int IN_OPER = 39;
  int LESS_THAN = 40;
  int LESS_THAN_EQUAL = 41;
  int GREATER_THAN = 42;
  int GREATER_THAN_EQUAL = 43;
  int REGEX_EQUAL = 44;
  int CMPOPERATOR = 45;
  int MINUS = 46;
  int PLUS = 47;
  int MULTIPLY = 48;
  int DIVIDE = 49;
  int MODULO = 50;
  int INCR = 51;
  int DECR = 52;
  int TILDA = 53;
  int FIELD_ID = 54;
  int REC_NAME_FIELD_ID = 55;
  int REC_NAME_FIELD_NUM = 56;
  int REC_NUM_FIELD_ID = 57;
  int REC_NUM_FIELD_NUM = 58;
  int REC_NUM_WILDCARD = 59;
  int REC_NAME_WILDCARD = 60;
  int REC_NUM_ID = 61;
  int REC_NAME_ID = 62;
  int OPEN_PAR = 63;
  int CLOSE_PAR = 64;
  int INT_VAR = 65;
  int LONG_VAR = 66;
  int DATE_VAR = 67;
  int DOUBLE_VAR = 68;
  int DECIMAL_VAR = 69;
  int BOOLEAN_VAR = 70;
  int STRING_VAR = 71;
  int BYTE_VAR = 72;
  int LIST_VAR = 73;
  int MAP_VAR = 74;
  int RECORD_VAR = 75;
  int OBJECT_VAR = 76;
  int BREAK = 77;
  int CONTINUE = 78;
  int ELSE = 79;
  int FOR = 80;
  int FOR_EACH = 81;
  int FUNCTION = 82;
  int IF = 83;
  int RETURN = 84;
  int WHILE = 85;
  int CASE = 86;
  int ENUM = 87;
  int IMPORT = 88;
  int SWITCH = 89;
  int CASE_DEFAULT = 90;
  int DO = 91;
  int TRY = 92;
  int CATCH = 93;
  int RETURN_RECORD_SKIP = 94;
  int RETURN_RECORD_SEND_ALL = 95;
  int RETURN_RECORD_OK = 96;
  int YEAR = 97;
  int MONTH = 98;
  int WEEK = 99;
  int DAY = 100;
  int HOUR = 101;
  int MINUTE = 102;
  int SECOND = 103;
  int MILLISEC = 104;
  int IDENTIFIER = 105;
  int DATE_FIELD_LITERAL = 131;
  int ERROR = 132;

  int DEFAULT = 0;
  int WithinComment = 1;

  String[] tokenImage = {
    "<EOF>",
    "\" \"",
    "\"\\t\"",
    "\"\\n\"",
    "\"\\r\"",
    "\"\\n\\r\"",
    "\"/*\"",
    "\"*/\"",
    "<token of kind 8>",
    "<SINGLE_LINE_COMMENT>",
    "<INTEGER_LITERAL>",
    "<DIGIT>",
    "<LETTER>",
    "<UNDERSCORE>",
    "<DECIMAL_LITERAL>",
    "<HEX_LITERAL>",
    "<OCTAL_LITERAL>",
    "<FLOATING_POINT_LITERAL>",
    "<EXPONENT>",
    "<STRING_LITERAL>",
    "<DQUOTED_STRING>",
    "<UNTERMINATED_STRING_LITERAL>",
    "<UNTERMINATED_DQUOTED_STRING>",
    "<BOOLEAN_LITERAL>",
    "\"true\"",
    "\"false\"",
    "<DATE_LITERAL>",
    "<DATETIME_LITERAL>",
    "\";\"",
    "\"{\"",
    "\"}\"",
    "\"null\"",
    "\"\\\'\"",
    "\":=\"",
    "<OR>",
    "<AND>",
    "<NOT>",
    "<EQUAL>",
    "<NON_EQUAL>",
    "\".in.\"",
    "<LESS_THAN>",
    "<LESS_THAN_EQUAL>",
    "<GREATER_THAN>",
    "<GREATER_THAN_EQUAL>",
    "<REGEX_EQUAL>",
    "<CMPOPERATOR>",
    "\"-\"",
    "\"+\"",
    "\"*\"",
    "\"/\"",
    "\"%\"",
    "\"++\"",
    "\"--\"",
    "\"~\"",
    "<FIELD_ID>",
    "<REC_NAME_FIELD_ID>",
    "<REC_NAME_FIELD_NUM>",
    "<REC_NUM_FIELD_ID>",
    "<REC_NUM_FIELD_NUM>",
    "<REC_NUM_WILDCARD>",
    "<REC_NAME_WILDCARD>",
    "<REC_NUM_ID>",
    "<REC_NAME_ID>",
    "\"(\"",
    "\")\"",
    "\"int\"",
    "\"long\"",
    "\"date\"",
    "<DOUBLE_VAR>",
    "\"decimal\"",
    "\"boolean\"",
    "\"string\"",
    "\"bytearray\"",
    "\"list\"",
    "\"map\"",
    "\"record\"",
    "\"object\"",
    "\"break\"",
    "\"continue\"",
    "\"else\"",
    "\"for\"",
    "\"foreach\"",
    "\"function\"",
    "\"if\"",
    "\"return\"",
    "\"while\"",
    "\"case\"",
    "\"enum\"",
    "\"import\"",
    "\"switch\"",
    "\"default\"",
    "\"do\"",
    "\"try\"",
    "\"catch\"",
    "\"SKIP\"",
    "\"ALL\"",
    "\"OK\"",
    "\"year\"",
    "\"month\"",
    "\"week\"",
    "\"day\"",
    "\"hour\"",
    "\"minute\"",
    "\"second\"",
    "\"millisec\"",
    "<IDENTIFIER>",
    "\",\"",
    "\"=\"",
    "\":\"",
    "\"[\"",
    "\"]\"",
    "\"isnull(\"",
    "\"nvl(\"",
    "\"nvl2(\"",
    "\"iif(\"",
    "\"print_stack(\"",
    "\"breakpoint(\"",
    "\"raise_error(\"",
    "\"print_err(\"",
    "\"eval(\"",
    "\"eval_exp(\"",
    "\"print_log(\"",
    "\"sequence(\"",
    "\".next\"",
    "\".current\"",
    "\".reset\"",
    "\"lookup(\"",
    "\".\"",
    "\"lookup_next(\"",
    "\"lookup_found(\"",
    "\"lookup_admin(\"",
    "<DATE_FIELD_LITERAL>",
    "<ERROR>",
  };

}
