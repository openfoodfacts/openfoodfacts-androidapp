# -----------------------------------------------------------------------------
#
# Blockenspiel dynamic tests
#
# This file contains tests for dynamic DSL generation.
#
# -----------------------------------------------------------------------------
# Copyright 2008-2011 Daniel Azuma
#
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# * Redistributions of source code must retain the above copyright notice,
#   this list of conditions and the following disclaimer.
# * Redistributions in binary form must reproduce the above copyright notice,
#   this list of conditions and the following disclaimer in the documentation
#   and/or other materials provided with the distribution.
# * Neither the name of the copyright holder, nor the names of any other
#   contributors to this software, may be used to endorse or promote products
#   derived from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
# ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
# LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
# CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
# SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
# INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
# CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
# ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
# POSSIBILITY OF SUCH DAMAGE.
# -----------------------------------------------------------------------------
;


require 'minitest/autorun'
require 'blockenspiel'


module Blockenspiel
  module Tests  # :nodoc:

    class TestDynamic < ::Minitest::Test  # :nodoc:


      # Test the simple case.
      #
      # * Asserts that the simplest case works.

      def test_simple
        block_ = ::Proc.new do
          set_value(:a, 1)
        end
        hash_ = ::Hash.new
        ::Blockenspiel.invoke(block_) do
          add_method(:set_value) do |key_, value_|
            hash_[key_] = value_
          end
        end
        assert_equal(1, hash_[:a])
      end


      # Test renaming.
      #
      # * Asserts that the method appears renamed in a parameterless block.
      # * Asserts that the method appears in its original name in a parametered block.

      def test_renaming
        hash_ = ::Hash.new
        dsl_definition_ = ::Proc.new do
          add_method(:set_value, :dsl_method => :renamed_set_value) do |key_, value_|
            hash_[key_] = value_
          end
        end
        block1_ = ::Proc.new do
          renamed_set_value(:a, 1)
          assert_raises(::NoMethodError){ set_value(:b, 2) }
        end
        ::Blockenspiel.invoke(block1_, &dsl_definition_)
        block2_ = ::Proc.new do |dsl_|
          dsl_.set_value(:c, 3)
          assert_raises(::NoMethodError){ renamed_set_value(:d, 4) }
        end
        ::Blockenspiel.invoke(block2_, &dsl_definition_)
        assert_equal(1, hash_[:a])
        assert_nil(hash_[:b])
        assert_equal(3, hash_[:c])
        assert_nil(hash_[:d])
      end


      # Test calls with blocks passed the usual way.
      # Note: this will fail in MRI < 1.8.7 and JRuby < 1.5.
      #
      # * Asserts that a block passed the usual way works
      # * Asserts that we can detect when a block has not been passed

      def test_blocks_normal
        hash_ = ::Hash.new
        block_ = ::Proc.new do
          set_value1(:a){ 1 }
          set_value2(:b){ 2 }
          set_value2(:c)
        end
        ::Blockenspiel.invoke(block_) do
          add_method(:set_value1) do |key_, &bl_|
            hash_[key_] = bl_.call
          end
          add_method(:set_value2) do |key_, &bl_|
            hash_[key_] = bl_ ? true : false
          end
        end
        assert_equal(1, hash_[:a])
        assert_equal(true, hash_[:b])
        assert_equal(false, hash_[:c])
      end


      # Test calls with blocks passed as non-block parameters.
      #
      # * Asserts that a block passed "first" works.
      # * Asserts that a block passed "last" works.
      # * Asserts that a block passed "true" works.

      def test_blocks_first_and_last
        hash_ = ::Hash.new
        block_ = ::Proc.new do
          set_value1(:a){ 1 }
          set_value2(:b){ 2 }
          set_value2(:c){ 3 }
        end
        ::Blockenspiel.invoke(block_) do
          add_method(:set_value1, :block => :first) do |bl_, key_|
            hash_[key_] = bl_.call
          end
          add_method(:set_value2, :block => :last) do |key_, bl_|
            hash_[key_] = bl_.call
          end
          add_method(:set_value3, :block => true) do |bl_, key_|
            hash_[key_] = bl_.call
          end
        end
        assert_equal(1, hash_[:a])
        assert_equal(2, hash_[:b])
        assert_equal(3, hash_[:c])
      end


      # Test calls with blocks not passed.
      #
      # * Asserts that if a block isn't given, it is set to nil.

      def test_blocks_nil
        hash_ = ::Hash.new
        block_ = ::Proc.new do
          set_value1(:a)
          set_value2(:b)
        end
        ::Blockenspiel.invoke(block_) do
          add_method(:set_value1, :block => :first) do |bl_, key_|
            assert_nil(bl_)
          end
          add_method(:set_value2, :block => :last) do |key_, bl_|
            assert_nil(bl_)
          end
        end
        assert_nil(hash_[:a])
        assert_nil(hash_[:b])
      end


      # Test calls with blocks (legacy api)
      #
      # * Asserts that a block with receive_block works.

      def test_blocks_legacy
        hash_ = ::Hash.new
        block_ = ::Proc.new do
          set_value(:a){ 1 }
        end
        ::Blockenspiel.invoke(block_) do
          add_method(:set_value, :receive_block => true) do |key_, bl_|
            hash_[key_] = bl_.call
          end
        end
        assert_equal(1, hash_[:a])
      end


      # Test passing options in.
      #
      # * Asserts that the "parameter" option is recognized

      def test_options_recognized
        block_ = ::Proc.new do
          set_value(:a, 1)
        end
        hash_ = ::Hash.new
        assert_raises(::Blockenspiel::BlockParameterError) do
          ::Blockenspiel.invoke(block_, :parameterless => false) do
            add_method(:set_value) do |key_, value_|
              hash_[key_] = value_
            end
          end
        end
      end


    end

  end
end
