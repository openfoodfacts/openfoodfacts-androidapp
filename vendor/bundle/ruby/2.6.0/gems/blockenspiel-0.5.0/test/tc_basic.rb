# -----------------------------------------------------------------------------
#
# Blockenspiel basic tests
#
# This file contains tests for the simple use cases.
#
# -----------------------------------------------------------------------------
# Copyright 2008 Daniel Azuma
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

    class TestBasic < ::Minitest::Test  # :nodoc:


      class SimpleTarget < ::Blockenspiel::Base

        def initialize
          @hash = ::Hash.new
        end

        def set_value(key_, value_)
          @hash[key_] = value_
        end

        def set_value_by_block(key_)
          @hash[key_] = yield
        end

        def get_value(key_)
          @hash[key_]
        end
        dsl_method :get_value, false

      end


      # Test basic usage with a parameter object.
      #
      # * Asserts that methods are not mixed in to self.
      # * Asserts that the specified target object does in fact receive the block messages.

      def test_basic_param
        block_ = ::Proc.new do |t_|
          t_.set_value(:a, 1)
          t_.set_value_by_block(:b){ 2 }
          assert(!self.respond_to?(:set_value))
          assert(!self.respond_to?(:set_value_by_block))
        end
        target_ = SimpleTarget.new
        ::Blockenspiel.invoke(block_, target_)
        assert_equal(1, target_.get_value(:a))
        assert_equal(2, target_.get_value(:b))
      end


      # Test basic usage with a delegator.
      #
      # * Asserts that the specified target object receives the messages.
      # * Asserts that methods from the surrounding context are also available.
      # * Asserts that methods are not present in self afterward.

      def test_basic_parameterless
        block_ = ::Proc.new do
          set_value(:a, 1)
          set_value_by_block(:b){ 2 }
          assert(true)
        end
        target_ = SimpleTarget.new
        ::Blockenspiel.invoke(block_, target_)
        assert(!self.respond_to?(:set_value))
        assert(!self.respond_to?(:set_value_by_block))
        assert_equal(1, target_.get_value(:a))
        assert_equal(2, target_.get_value(:b))
      end


      # Test basic usage with a builder.
      #
      # * Asserts that the receivers are called.
      # * Asserts that receivers with blocks are handled properly.

      def test_basic_builder
        block_ = ::Proc.new do
          set_value(:a, 1)
          set_value_by_block(:b){ 2 }
        end
        hash_ = ::Hash.new
        ::Blockenspiel.invoke(block_) do
          add_method(:set_value) do |key_, value_|
            hash_[key_] = value_
          end
          add_method(:set_value_by_block, :block => true) do |bl_, key_|
            hash_[key_] = bl_.call
          end
        end
        assert_equal(1, hash_[:a])
        assert_equal(2, hash_[:b])
      end


      # Test basic usage with a string.
      #
      # * Asserts that the specified target object receives the messages.

      def test_basic_string
        string_ = <<-STR
          set_value(:a, 1)
          set_value_by_block(:b){ 2 }
        STR
        target_ = SimpleTarget.new
        ::Blockenspiel.invoke(string_, target_)
        assert(!self.respond_to?(:set_value))
        assert_equal(1, target_.get_value(:a))
        assert_equal(2, target_.get_value(:b))
      end


      # Test basic usage with a file.
      #
      # * Asserts that the specified target object receives the messages.

      def test_basic_file
        target_ = SimpleTarget.new
        ::Blockenspiel.invoke(target_, :file => "#{File.dirname(__FILE__)}/files/file1.rb")
        assert(!self.respond_to?(:set_value))
        assert_equal(1, target_.get_value(:a))
        assert_equal(2, target_.get_value(:b))
      end


    end

  end
end
