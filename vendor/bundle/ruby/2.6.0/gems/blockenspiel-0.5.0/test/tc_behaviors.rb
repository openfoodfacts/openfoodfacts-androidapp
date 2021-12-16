# -----------------------------------------------------------------------------
#
# Blockenspiel behavior tests
#
# This file contains tests for behavior settings.
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

    class TestBehaviors < ::Minitest::Test  # :nodoc:


      class Target1 < ::Blockenspiel::Base

        dsl_methods false

        def initialize(hash_)
          @hash = hash_
        end

        def set_value1(key_, value_)
          @hash["#{key_}1"] = value_
        end
        dsl_method :set_value1

        def set_value2(key_)
          @hash["#{key_}2"] = yield
        end
        dsl_method :set_value2

        def set_value3(key_, value_)
          @hash["#{key_}3"] = value_
        end
        dsl_method :set_value3_dslversion, :set_value3

      end


      def helper_method
        true
      end


      # Test instance_eval behavior.
      #
      # * Asserts that self points at the target.
      # * Asserts that the target methods are available.
      # * Asserts that the target methods are not renamed by dsl_method directives.
      # * Asserts that the caller's instance variables are not available.
      # * Asserts that the caller's helper methods are not available.

      def test_instance_eval_behavior
        hash_ = ::Hash.new
        context_self_ = self
        @my_instance_variable_test = :hello
        block_ = ::Proc.new do
          set_value1('a', 1)
          set_value2('b'){ 2 }
          set_value3('c', 3)
          context_self_.assert_raises(::NoMethodError){ set_value3_dslversion('d', 4) }
          context_self_.assert_raises(::NoMethodError){ helper_method() }
          context_self_.assert(!instance_variable_defined?(:@my_instance_variable_test))
          context_self_.assert_instance_of(::Blockenspiel::Tests::TestBehaviors::Target1, self)
        end
        ::Blockenspiel.invoke(block_, Target1.new(hash_), :parameterless => :instance)
        assert_equal(1, hash_['a1'])
        assert_equal(2, hash_['b2'])
        assert_equal(3, hash_['c3'])
      end


      # Test proxy behavior.
      #
      # * Asserts that self doesn't point at the Target nor the original context.
      # * Asserts that the target methods are available in their dsl renamed forms.
      # * Asserts that the caller's instance variables are not available.
      # * Asserts that the caller's helper methods *are* available.

      def test_proxy_behavior
        hash_ = ::Hash.new
        context_self_ = self
        @my_instance_variable_test = :hello
        block_ = ::Proc.new do
          set_value1('a', 1)
          set_value2('b'){ 2 }
          set_value3_dslversion('c', 3)
          context_self_.assert_raises(::NoMethodError){ set_value3('d', 4) }
          context_self_.assert(helper_method())
          context_self_.assert(!instance_variable_defined?(:@my_instance_variable_test))
          context_self_.assert(!self.kind_of?(::Blockenspiel::Tests::TestBehaviors::Target1))
          context_self_.refute_equal(context_self_, self)
        end
        ::Blockenspiel.invoke(block_, Target1.new(hash_), :parameterless => :proxy)
        assert_equal(1, hash_['a1'])
        assert_equal(2, hash_['b2'])
        assert_equal(3, hash_['c3'])
      end


      # Test parameterless blocks disabled
      #
      # * Asserts that an error is raised if sending a no-parameter block in this case.
      # * Asserts that sending a one-parameter block still works.

      def test_disable_parameterless
        hash_ = ::Hash.new
        block1_ = ::Proc.new do ||
          set_value1('a', 1)
        end
        block2_ = ::Proc.new do |target_|
          target_.set_value1('b', 2)
        end
        block3_ = ::Proc.new do
          set_value1('c', 3)
        end
        assert_raises(::Blockenspiel::BlockParameterError) do
          ::Blockenspiel.invoke(block1_, Target1.new(hash_), :parameterless => false)
        end
        ::Blockenspiel.invoke(block2_, Target1.new(hash_), :parameterless => false)
        assert_raises(::Blockenspiel::BlockParameterError) do
          ::Blockenspiel.invoke(block3_, Target1.new(hash_), :parameterless => false)
        end
        assert_equal(2, hash_['b1'])
      end


      # Test parametered blocks disabled
      #
      # * Asserts that an error is raised if sending a one-parameter block in this case.
      # * Asserts that sending a no-parameter block still works.

      def test_disable_parametered
        hash_ = ::Hash.new
        block1_ = ::Proc.new do ||
          set_value1('a', 1)
        end
        block2_ = ::Proc.new do |target_|
          target_.set_value1('b', 2)
        end
        block3_ = ::Proc.new do
          set_value1('c', 3)
        end
        ::Blockenspiel.invoke(block1_, Target1.new(hash_), :parameter => false)
        assert_raises(::Blockenspiel::BlockParameterError) do
          ::Blockenspiel.invoke(block2_, Target1.new(hash_), :parameter => false)
        end
        ::Blockenspiel.invoke(block3_, Target1.new(hash_), :parameter => false)
        assert_equal(1, hash_['a1'])
        assert_equal(3, hash_['c1'])
      end

    end

  end
end
