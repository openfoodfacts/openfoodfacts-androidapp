# -----------------------------------------------------------------------------
#
# Blockenspiel dsl method tests
#
# This file contains tests for the dsl method directives.
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

    class TestDSLMethods < ::Minitest::Test  # :nodoc:


      class Target1 < ::Blockenspiel::Base

        def initialize(hash_)
          @hash = hash_
        end

        def set_value1(key_, value_)
          @hash["#{key_}1"] = value_
        end

        def set_value2(key_)
          @hash["#{key_}2"] = yield
        end

        def _set_value3(key_, value_)
          @hash["#{key_}3"] = value_
        end

      end


      class Target2 < ::Blockenspiel::Base

        def initialize(hash_)
          @hash = hash_
        end

        dsl_methods false

        def set_value1(key_, value_)
          @hash["#{key_}1"] = value_
        end

        dsl_methods true

        def set_value2(key_)
          @hash["#{key_}2"] = yield
        end

        def _set_value3(key_, value_)
          @hash["#{key_}3"] = value_
        end

      end


      class Target3 < ::Blockenspiel::Base

        def initialize(hash_)
          @hash = hash_
        end

        dsl_methods false

        def set_value1(key_, value_)
          @hash["#{key_}1"] = value_
        end
        dsl_method :set_value1

        def set_value2(key_)
          @hash["#{key_}2"] = yield
        end
        dsl_method :renamed_set_value2, :set_value2
        dsl_method :another_set_value2, :set_value2

      end


      class Target4 < ::Blockenspiel::Base

        def initialize(hash_)
          @hash = hash_
        end

        def set_value1(key_, value_)
          @hash["#{key_}1"] = value_
        end
        dsl_method :set_value1, false

        def set_value2(key_)
          @hash["#{key_}2"] = yield
        end
        dsl_method :set_value2, false
        dsl_method :renamed_set_value2, :set_value2

      end


      class Target5a < ::Blockenspiel::Base

        def initialize(hash_)
          @hash = hash_
        end

        def set_value1(key_, value_)
          @hash["#{key_}1"] = value_
        end

        def set_value2(key_)
          @hash["#{key_}2"] = yield
        end

        def set_value3(key_, value_)
          @hash["#{key_}3"] = value_
        end

        def set_value4(key_, value_)
          @hash["#{key_}4"] = value_
        end
        dsl_method :set_value4, false
        dsl_method :renamed_set_value4, :set_value4

      end


      class Target5b < Target5a

        def set_value1(key_, value_)
          @hash["#{key_}1sub"] = value_
        end

        dsl_method :set_value3, false

        def set_value5(key_, value_)
          @hash["#{key_}5"] = value_
        end

      end


      class Target6 < ::Blockenspiel::Base

        def initialize(hash_)
          @hash = hash_
        end

        dsl_methods false

        def set_value1(key_, value_)
          @hash["#{key_}1"] = value_
        end

        def set_value2(key_)
          @hash["#{key_}2"] = yield
        end

        dsl_methods :set_value1, :renamed_set_value2 => :set_value2

      end


      # Test default dsl method setting.
      #
      # * Asserts the right dsl methods are added for the default setting.

      def test_default_setting
        hash_ = ::Hash.new
        block_ = ::Proc.new do
          set_value1('a', 1)
          set_value2('b'){ 2 }
          assert_raises(::NoMethodError){ _set_value3('c', 3) }
        end
        ::Blockenspiel.invoke(block_, Target1.new(hash_))
        assert_equal(1, hash_['a1'])
        assert_equal(2, hash_['b2'])
        assert_nil(hash_['c3'])
      end


      # Test dsl_methods true and false switching.
      #
      # * Asserts that dsl_methods false turns off automatic method creation.
      # * Asserts that dsl_methods true turns on automatic method creation.
      # * Asserts that underscore methods are added in dsl_methods true mode.

      def test_onoff_switching
        hash_ = ::Hash.new
        block_ = ::Proc.new do
          assert_raises(::NoMethodError){ _set_value1('a', 1) }
          set_value2('b'){ 2 }
          _set_value3('c', 3)
        end
        ::Blockenspiel.invoke(block_, Target2.new(hash_))
        assert_nil(hash_['a1'])
        assert_equal(2, hash_['b2'])
        assert_equal(3, hash_['c3'])
      end


      # Test dsl_methods explicit adding.
      #
      # * Asserts that adding an explicit dsl method works.
      # * Asserts that adding an explicit dsl method with a different name works.

      def test_explicit_add
        hash_ = ::Hash.new
        block_ = ::Proc.new do
          set_value1('a', 1)
          assert_raises(::NoMethodError){ set_value2('b'){ 2 } }
          renamed_set_value2('c'){ 3 }
          another_set_value2('d'){ 4 }
        end
        ::Blockenspiel.invoke(block_, Target3.new(hash_))
        assert_equal(1, hash_['a1'])
        assert_nil(hash_['b2'])
        assert_equal(3, hash_['c2'])
        assert_equal(4, hash_['d2'])
      end


      # Test dsl_methods explicit removing.
      #
      # * Asserts that removing a dsl method works.
      # * Asserts that re-adding a removed method with a different name works.

      def test_explicit_removing
        hash_ = ::Hash.new
        block_ = ::Proc.new do
          assert_raises(::NoMethodError){ set_value1('a', 1) }
          assert_raises(::NoMethodError){ set_value2('b'){ 2 } }
          renamed_set_value2('c'){ 3 }
        end
        ::Blockenspiel.invoke(block_, Target4.new(hash_))
        assert_nil(hash_['a1'])
        assert_nil(hash_['b2'])
        assert_equal(3, hash_['c2'])
      end


      # Test dsl method setting with subclasses
      #
      # * Asserts that modules are properly inherited.
      # * Asserts that method overriding is done correctly.

      def test_subclassing
        hash_ = ::Hash.new
        block_ = ::Proc.new do
          set_value1('a', 1)
          set_value2('b'){ 2 }
          assert_raises(::NoMethodError){ set_value3('c', 3) }
          assert_raises(::NoMethodError){ set_value4('d', 4) }
          renamed_set_value4('e', 5)
          set_value5('f', 6)
        end
        ::Blockenspiel.invoke(block_, Target5b.new(hash_))
        assert_equal(1, hash_['a1sub'])
        assert_equal(2, hash_['b2'])
        assert_nil(hash_['c3'])
        assert_nil(hash_['d4'])
        assert_equal(5, hash_['e4'])
        assert_equal(6, hash_['f5'])
      end


      # Test dsl_methods with multiple parameters.
      #
      # * Asserts that hash syntax for dsl_methods works.
      # * Asserts that combined array and hash parameters works.

      def test_multiple_dsl_methods
        hash_ = ::Hash.new
        block_ = ::Proc.new do
          set_value1('a', 1)
          renamed_set_value2('b'){ 2 }
          assert_raises(::NoMethodError){ set_value2('c', 3) }
        end
        ::Blockenspiel.invoke(block_, Target6.new(hash_))
        assert_equal(1, hash_['a1'])
        assert_equal(2, hash_['b2'])
      end


    end

  end
end
