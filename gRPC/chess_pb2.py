# -*- coding: utf-8 -*-
# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: chess.proto
# Protobuf Python Version: 5.26.1
"""Generated protocol buffer code."""
from google.protobuf import descriptor as _descriptor
from google.protobuf import descriptor_pool as _descriptor_pool
from google.protobuf import symbol_database as _symbol_database
from google.protobuf.internal import builder as _builder
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()




DESCRIPTOR = _descriptor_pool.Default().AddSerializedFile(b'\n\x0b\x63hess.proto\x12\x05\x63hess\"W\n\x12GetBestMoveRequest\x12\x0b\n\x03\x66\x65n\x18\x01 \x01(\t\x12\x12\n\nmove_stack\x18\x02 \x03(\t\x12\r\n\x05\x64\x65pth\x18\x03 \x01(\x05\x12\x11\n\tprocesses\x18\x04 \x01(\x05\"J\n\x13GetBestMoveResponse\x12\x11\n\tbest_move\x18\x01 \x01(\t\x12\x12\n\ntime_taken\x18\x02 \x01(\x05\x12\x0c\n\x04\x65val\x18\x03 \x01(\x05\x32S\n\x0b\x43hessEngine\x12\x44\n\x0bGetBestMove\x12\x19.chess.GetBestMoveRequest\x1a\x1a.chess.GetBestMoveResponseb\x06proto3')

_globals = globals()
_builder.BuildMessageAndEnumDescriptors(DESCRIPTOR, _globals)
_builder.BuildTopDescriptorsAndMessages(DESCRIPTOR, 'chess_pb2', _globals)
if not _descriptor._USE_C_DESCRIPTORS:
  DESCRIPTOR._loaded_options = None
  _globals['_GETBESTMOVEREQUEST']._serialized_start=22
  _globals['_GETBESTMOVEREQUEST']._serialized_end=109
  _globals['_GETBESTMOVERESPONSE']._serialized_start=111
  _globals['_GETBESTMOVERESPONSE']._serialized_end=185
  _globals['_CHESSENGINE']._serialized_start=187
  _globals['_CHESSENGINE']._serialized_end=270
# @@protoc_insertion_point(module_scope)
