# -----------------------------------------------------------------------------
#
# Blockenspiel mixin tests
#
# This file contains tests for various mixin cases,
# including nested blocks and multithreading.
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

    class TestMixins < ::Minitest::Test  # :nodoc:


      class Target1 < ::Blockenspiel::Base

        def initialize(hash_)
          @hash = hash_
        end

        def set_value(key_, value_)
          @hash["#{key_}1"] = value_
        end

        def set_value2(key_)
          @hash["#{key_}1"] = yield
        end

      end


      class Target2 < ::Blockenspiel::Base

        dsl_methods false

        def initialize(hash_=nil)
          @hash = hash_ || ::Hash.new
        end

        def set_value(key_, value_)
          @hash["#{key_}2"] = value_
        end
        dsl_method :set_value

        def set_value2(key_)
          @hash["#{key_}2"] = yield
        end
        dsl_method :set_value2_inmixin, :set_value2

      end


      class Target3 < ::Blockenspiel::Base

        def initialize(hash_)
          @hash = hash_
        end

        def set_value(key_, value_)
          @hash[key_] = value_
        end

        def _helper_method(key_)
          @hash[key_]
        end

        def get_value(key_)
          @hash[key_]
        end
        dsl_method :get_value, false

        def get_value2(key_)
          @hash[key_]
        end
        dsl_method :get_value2, false

      end


      def get_value(key_)
        :helper
      end


      # Basic test of mixin mechanism.
      #
      # * Asserts that the mixin methods are added and removed for a single mixin.
      # * Asserts that the methods properly delegate to the target object.
      # * Asserts that self doesn't change, and instance variables are preserved.

      def test_basic_mixin
        skip unless ::Blockenspiel.mixin_available?

        hash_ = ::Hash.new
        saved_object_id_ = self.object_id
        @my_instance_variable_test = :hello
        assert(!self.respond_to?(:set_value))
        assert(!self.respond_to?(:set_value2))
        block_ = ::Proc.new do
          set_value('a', 1)
          set_value2('b'){ 2 }
          assert_equal(:hello, @my_instance_variable_test)
          assert_equal(saved_object_id_, self.object_id)
        end
        ::Blockenspiel.invoke(block_, Target1.new(hash_), :parameterless => :mixin)
        assert(!self.respond_to?(:set_value))
        assert(!self.respond_to?(:set_value2))
        assert_equal(1, hash_['a1'])
        assert_equal(2, hash_['b1'])
      end


      # Test renaming of mixin methods.
      #
      # * Asserts that correctly renamed mixin methods are added and removed.
      # * Asserts that the methods properly delegate to the target object.

      def test_mixin_with_renaming
        skip unless ::Blockenspiel.mixin_available?

        hash_ = ::Hash.new
        assert(!self.respond_to?(:set_value))
        assert(!self.respond_to?(:set_value2))
        assert(!self.respond_to?(:set_value2_inmixin))
        block_ = ::Proc.new do
          set_value('a', 1)
          set_value2_inmixin('b'){ 2 }
          assert(!self.respond_to?(:set_value2))
        end
        ::Blockenspiel.invoke(block_, Target2.new(hash_), :parameterless => :mixin)
        assert(!self.respond_to?(:set_value))
        assert(!self.respond_to?(:set_value2))
        assert(!self.respond_to?(:set_value2_inmixin))
        assert_equal(1, hash_['a2'])
        assert_equal(2, hash_['b2'])
      end


      # Test of two different nested mixins.
      #
      # * Asserts that the right methods are added and removed at the right time.
      # * Asserts that the methods delegate to the right target object, even when
      #   multiple mixins add the same method name

      def test_nested_different
        skip unless ::Blockenspiel.mixin_available?

        hash_ = ::Hash.new
        assert(!self.respond_to?(:set_value))
        assert(!self.respond_to?(:set_value2))
        assert(!self.respond_to?(:set_value2_inmixin))
        ::Blockenspiel.invoke(::Proc.new do
          set_value('a', 1)
          set_value2('b'){ 2 }
          assert(!self.respond_to?(:set_value2_inmixin))
          ::Blockenspiel.invoke(::Proc.new do
            set_value('c', 1)
            set_value2_inmixin('d'){ 2 }
          end, Target2.new(hash_), :parameterless => :mixin)
          assert(!self.respond_to?(:set_value2_inmixin))
          set_value('e', 1)
          set_value2('f'){ 2 }
        end, Target1.new(hash_), :parameterless => :mixin)
        assert(!self.respond_to?(:set_value))
        assert(!self.respond_to?(:set_value2))
        assert(!self.respond_to?(:set_value2_inmixin))
        assert_equal(1, hash_['a1'])
        assert_equal(2, hash_['b1'])
        assert_equal(1, hash_['c2'])
        assert_equal(2, hash_['d2'])
        assert_equal(1, hash_['e1'])
        assert_equal(2, hash_['f1'])
      end


      # Test of the same mixin nested in itself.
      #
      # * Asserts that the methods are added and removed at the right time.

      def test_nested_same
        skip unless ::Blockenspiel.mixin_available?

        hash_ = ::Hash.new
        assert(!self.respond_to?(:set_value))
        assert(!self.respond_to?(:set_value2))
        assert(!self.respond_to?(:set_value2_inmixin))
        ::Blockenspiel.invoke(::Proc.new do
          set_value('a', 1)
          set_value2_inmixin('b'){ 2 }
          ::Blockenspiel.invoke(::Proc.new do
            set_value('c', 1)
            set_value2_inmixin('d'){ 2 }
            assert(!self.respond_to?(:set_value2))
          end, Target2.new(hash_), :parameterless => :mixin)
          set_value('e', 1)
          set_value2_inmixin('f'){ 2 }
        end, Target2.new(hash_), :parameterless => :mixin)
        assert(!self.respond_to?(:set_value))
        assert(!self.respond_to?(:set_value2))
        assert(!self.respond_to?(:set_value2_inmixin))
        assert_equal(1, hash_['a2'])
        assert_equal(2, hash_['b2'])
        assert_equal(1, hash_['c2'])
        assert_equal(2, hash_['d2'])
        assert_equal(1, hash_['e2'])
        assert_equal(2, hash_['f2'])
      end


      # Test of two threads mixing the same mixin into the same object
      #
      # * Asserts that the mixin is removed only after the second thread is done.

      def test_threads_same_mixin
        skip unless ::Blockenspiel.mixin_available?

        hash_ = ::Hash.new
        block1_ = ::Proc.new do
          set_value('a', 1)
          sleep(0.1)
          set_value2('b'){ 2 }
        end
        block2_ = ::Proc.new do
          set_value('c', 3)
          sleep(0.2)
          set_value2('d'){ 4 }
        end
        target_ = Target1.new(hash_)
        thread1_ = ::Thread.new do
          ::Blockenspiel.invoke(block1_, target_, :parameterless => :mixin)
        end
        thread2_ = ::Thread.new do
          ::Blockenspiel.invoke(block2_, target_, :parameterless => :mixin)
        end
        thread1_.join
        thread2_.join
        assert_equal(1, hash_['a1'])
        assert_equal(2, hash_['b1'])
        assert_equal(3, hash_['c1'])
        assert_equal(4, hash_['d1'])
      end


      def test_two_threads_different_mixin
        skip unless ::Blockenspiel.mixin_available?

        hash_ = {}
        target1_ = Target1.new(hash_)
        target2_ = Target2.new(hash_)
        assert(!self.respond_to?(:set_value))
        assert(!self.respond_to?(:set_value2))
        assert(!self.respond_to?(:set_value2_inmixin))
        t1_ = ::Thread.new do
          ::Blockenspiel.invoke(::Proc.new do
            sleep(0.1)
            set_value('a', 1)
            sleep(0.1)
            set_value('e', 5)
          end, target1_, :parameterless => :mixin)
        end
        t2_ = ::Thread.new do
          ::Blockenspiel.invoke(::Proc.new do
            sleep(0.1)
            set_value('A', 11)
            sleep(0.1)
            set_value('E', 15)
          end, target2_, :parameterless => :mixin)
        end
        t1_.join
        t2_.join
        assert(!self.respond_to?(:set_value))
        assert(!self.respond_to?(:set_value2))
        assert(!self.respond_to?(:set_value2_inmixin))
        assert_equal(1, hash_['a1'])
        assert_equal(5, hash_['e1'])
        assert_equal(11, hash_['A2'])
        assert_equal(15, hash_['E2'])
      end


      # A full thread test with the same set of nested mixins done into the same
      # object twice in two different threads.
      #
      # * Asserts that the right methods are added and removed at the right time.
      # * Asserts that the methods delegate to the right target object, even when
      #   multiple mixins add the same method name, multiple times from different
      #   threads.

      def test_nested_two_threads
        skip unless ::Blockenspiel.mixin_available?

        hash_ = {}
        target1_ = Target1.new(hash_)
        target2_ = Target2.new(hash_)
        assert(!self.respond_to?(:set_value))
        assert(!self.respond_to?(:set_value2))
        assert(!self.respond_to?(:set_value2_inmixin))
        t1_ = ::Thread.new do
          ::Blockenspiel.invoke(::Proc.new do
            sleep(0.1)
            set_value('a', 1)
            set_value2('b'){ 2 }
            ::Blockenspiel.invoke(::Proc.new do
              sleep(0.1)
              set_value('c', 3)
              set_value2_inmixin('d'){ 4 }
            end, target2_, :parameterless => :mixin)
            sleep(0.1)
            set_value('e', 5)
            set_value2('f'){ 6 }
          end, target1_, :parameterless => :mixin)
        end
        t2_ = ::Thread.new do
          ::Blockenspiel.invoke(::Proc.new do
            sleep(0.1)
            set_value('A', 11)
            set_value2_inmixin('B'){ 12 }
            ::Blockenspiel.invoke(::Proc.new do
              sleep(0.1)
              set_value('C', 13)
              set_value2('D'){ 14 }
            end, target1_, :parameterless => :mixin)
            sleep(0.1)
            set_value('E', 15)
            set_value2_inmixin('F'){ 16 }
          end, target2_, :parameterless => :mixin)
        end
        t1_.join
        t2_.join
        assert(!self.respond_to?(:set_value))
        assert(!self.respond_to?(:set_value2))
        assert(!self.respond_to?(:set_value2_inmixin))
        assert_equal(1, hash_['a1'])
        assert_equal(2, hash_['b1'])
        assert_equal(3, hash_['c2'])
        assert_equal(4, hash_['d2'])
        assert_equal(5, hash_['e1'])
        assert_equal(6, hash_['f1'])
        assert_equal(11, hash_['A2'])
        assert_equal(12, hash_['B2'])
        assert_equal(13, hash_['C1'])
        assert_equal(14, hash_['D1'])
        assert_equal(15, hash_['E2'])
        assert_equal(16, hash_['F2'])
      end


      # A full fiber test with the same set of nested mixins done into the same
      # object twice in two different fibers.
      #
      # * Asserts that the right methods are added and removed at the right time.
      # * Asserts that the methods delegate to the right target object, even when
      #   multiple mixins add the same method name, multiple times from different
      #   fibers.

      if defined?(::Fiber)

        def test_nested_two_fibers
          skip unless ::Blockenspiel.mixin_available?

          hash_ = {}
          target1_ = Target1.new(hash_)
          target2_ = Target2.new(hash_)
          assert(!self.respond_to?(:set_value))
          assert(!self.respond_to?(:set_value2))
          assert(!self.respond_to?(:set_value2_inmixin))
          f1_ = ::Fiber.new do
            ::Blockenspiel.invoke(::Proc.new do
              ::Fiber.yield
              set_value('a', 1)
              set_value2('b'){ 2 }
              ::Blockenspiel.invoke(::Proc.new do
                ::Fiber.yield
                set_value('c', 3)
                set_value2_inmixin('d'){ 4 }
              end, target2_, :parameterless => :mixin)
              ::Fiber.yield
              set_value('e', 5)
              set_value2('f'){ 6 }
            end, target1_, :parameterless => :mixin)
          end
          f2_ = ::Fiber.new do
            ::Blockenspiel.invoke(::Proc.new do
              ::Fiber.yield
              set_value('A', 11)
              set_value2_inmixin('B'){ 12 }
              ::Blockenspiel.invoke(::Proc.new do
                ::Fiber.yield
                set_value('C', 13)
                set_value2('D'){ 14 }
              end, target1_, :parameterless => :mixin)
              ::Fiber.yield
              set_value('E', 15)
              set_value2_inmixin('F'){ 16 }
            end, target2_, :parameterless => :mixin)
          end
          f1_.resume
          f2_.resume
          f1_.resume
          f2_.resume
          f1_.resume
          f2_.resume
          f1_.resume
          f2_.resume
          assert(!self.respond_to?(:set_value))
          assert(!self.respond_to?(:set_value2))
          assert(!self.respond_to?(:set_value2_inmixin))
          assert_equal(1, hash_['a1'])
          assert_equal(2, hash_['b1'])
          assert_equal(3, hash_['c2'])
          assert_equal(4, hash_['d2'])
          assert_equal(5, hash_['e1'])
          assert_equal(6, hash_['f1'])
          assert_equal(11, hash_['A2'])
          assert_equal(12, hash_['B2'])
          assert_equal(13, hash_['C1'])
          assert_equal(14, hash_['D1'])
          assert_equal(15, hash_['E2'])
          assert_equal(16, hash_['F2'])
        end

      end


      # Test mixin omissions.
      #
      # * Asserts that underscore methods are not mixed in.
      # * Asserts that methods that are turned off after the fact cannot be called.

      def test_omissions
        skip unless ::Blockenspiel.mixin_available?

        hash_ = ::Hash.new
        block_ = ::Proc.new do
          set_value(:a, 1)
          assert(!self.respond_to?(:_helper_method))
          assert_equal(:helper, get_value(:a))
          assert_raises(::NoMethodError) do
            get_value2(:a)
          end
        end
        target_ = Target3.new(hash_)
        ::Blockenspiel.invoke(block_, target_, :parameterless => :mixin)
        assert(!self.respond_to?(:set_value))
        assert_equal(1, target_.get_value(:a))
      end


    end

  end
end
