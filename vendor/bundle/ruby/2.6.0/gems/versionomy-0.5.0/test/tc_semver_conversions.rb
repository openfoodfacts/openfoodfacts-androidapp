# -----------------------------------------------------------------------------
#
# Versionomy tests on rubygems schema conversions
#
# This file contains tests converting to and from the rubygems schema
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


require 'minitest/autorun'
require 'versionomy'


module Versionomy
  module Tests  # :nodoc:

    class TestSemverConversions < ::Minitest::Test  # :nodoc:


      def setup
        @standard_format = Format.get(:standard)
        @semver_format = Format.get(:semver)
      end


      # Test simple conversion from standard to semver.

      def test_standard_to_semver_simple
        value_ = ::Versionomy.parse('1.2')
        value2_ = value_.convert(:semver)
        assert_equal(@semver_format, value2_.format)
        assert_equal([1, 2, 0, ''], value2_.values_array)
        value_ = ::Versionomy.parse('1.2.4')
        value2_ = value_.convert(:semver)
        assert_equal(@semver_format, value2_.format)
        assert_equal([1, 2, 4, ''], value2_.values_array)
      end


      # Test conversion from standard to semver with a beta version

      def test_standard_to_semver_beta
        value_ = ::Versionomy.parse('1.2b3')
        value2_ = value_.convert(:semver)
        assert_equal([1, 2, 0, 'b3'], value2_.values_array)
        value_ = ::Versionomy.parse('1.2 beta 3')
        value2_ = value_.convert(:semver)
        assert_equal([1, 2, 0, 'beta3'], value2_.values_array)
      end


      # Test conversion from standard to semver with a "v" prefix

      def test_standard_to_semver_with_v
        value_ = ::Versionomy.parse('v1.2.3')
        value2_ = value_.convert(:semver)
        assert_equal([1, 2, 3, ''], value2_.values_array)
        value_ = ::Versionomy.parse('V 1.2.3')
        value2_ = value_.convert(:semver)
        assert_equal([1, 2, 3, ''], value2_.values_array)
      end


      # Test conversion from standard to semver with an expectation of failure

      def test_standard_to_semver_fail
        value_ = ::Versionomy.parse('1.2.3.4', :standard)
        assert_raises(::Versionomy::Errors::ConversionError) do
          value_.convert(:semver)
        end
      end


      # Test simple conversion from semver to standard.

      def test_semver_to_standard_simple
        value_ = ::Versionomy.parse('1.2', :semver)
        value2_ = value_.convert(:standard)
        assert_equal(@standard_format, value2_.format)
        assert_equal([1, 2, 0, 0, :final, 0, 0], value2_.values_array)
        value_ = ::Versionomy.parse('1.2.4', :semver)
        value2_ = value_.convert(:standard)
        assert_equal(@standard_format, value2_.format)
        assert_equal([1, 2, 4, 0, :final, 0, 0], value2_.values_array)
      end


      # Test conversion from semver to standard with a beta version

      def test_semver_to_standard_beta
        value_ = ::Versionomy.parse('1.2.0b3', :semver)
        value2_ = value_.convert(:standard)
        assert_equal([1, 2, 0, 0, :beta, 3, 0], value2_.values_array)
        value_ = ::Versionomy.parse('1.2.4beta3', :semver)
        value2_ = value_.convert(:standard)
        assert_equal([1, 2, 4, 0, :beta, 3, 0], value2_.values_array)
      end


      # Test conversion from rubygems to standard with an expectation of failure

      def test_semver_to_standard_fail
        value_ = ::Versionomy.parse('1.2.3c4', :semver)
        assert_raises(::Versionomy::Errors::ConversionError) do
          value_.convert(:standard)
        end
      end


      # Test conversion when there aren't unparse_params

      def test_standard_to_semver_without_unparse_params
        value_ = ::Versionomy.create([1,2,3], :standard)
        value2_ = value_.convert(:semver)
        assert_equal([1, 2, 3, ''], value2_.values_array)
      end


      # Test conversion between semver and rubygems

      def test_semver_and_rubygems
        value_ = ::Versionomy.create([1,2,3], :semver)
        value2_ = value_.convert(:rubygems)
        assert_equal([1, 2, 3, 0, 0, 0, 0, 0], value2_.values_array)
        value_ = ::Versionomy.create([1,2,3], :rubygems)
        value2_ = value_.convert(:semver)
        assert_equal([1, 2, 3, ''], value2_.values_array)
      end


      # Test equality comparisons between semver and standard

      def test_semver_to_standard_equality_comparison
        assert_operator(::Versionomy.parse('1.2.0', :semver), :==, ::Versionomy.parse('1.2'))
        assert_operator(::Versionomy.parse('1.2.0b3', :semver), :==, ::Versionomy.parse('1.2b3'))
      end


      # Test inequality comparisons between semver and standard

      def test_semver_to_standard_inequality_comparison
        assert_operator(::Versionomy.parse('1.2.3', :semver), :<, ::Versionomy.parse('1.2.4'))
        assert_operator(::Versionomy.parse('1.2.0b3', :semver), :>, ::Versionomy.parse('1.2b2'))
        assert_operator(::Versionomy.parse('1.2.0', :semver), :>, ::Versionomy.parse('1.2b1'))
      end


      # Test equality comparisons between standard and rubygems

      def test_standard_to_semver_equality_comparison
        assert_operator(::Versionomy.parse('1.2.0'), :==, ::Versionomy.parse('1.2.0', :semver))
        assert_operator(::Versionomy.parse('1.2b3'), :==, ::Versionomy.parse('1.2.0beta3', :semver))
      end


      # Test inequality comparisons between standard and rubygems

      def test_standard_to_semver_inequality_comparison
        assert_operator(::Versionomy.parse('1.2.4'), :>, ::Versionomy.parse('1.2.3', :semver))
        assert_operator(::Versionomy.parse('1.2b2'), :<, ::Versionomy.parse('1.2.0beta3', :semver))
        assert_operator(::Versionomy.parse('1.2b2'), :<, ::Versionomy.parse('1.2.0', :semver))
      end


    end

  end
end
