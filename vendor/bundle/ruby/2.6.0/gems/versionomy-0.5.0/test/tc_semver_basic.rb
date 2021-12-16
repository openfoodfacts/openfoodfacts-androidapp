# -----------------------------------------------------------------------------
#
# Versionomy basic tests on rubygems schema
#
# This file contains tests for the basic use cases on the rubygems schema
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
require 'yaml'

module Versionomy
  module Tests  # :nodoc:

    class TestSemverBasic < ::Minitest::Test  # :nodoc:


      # Test the default version value.

      def test_default_value
        value_ = ::Versionomy.create(nil, :semver)
        assert_equal(1, value_.major)
        assert_equal(0, value_.minor)
        assert_equal(0, value_.patch)
        assert_equal('', value_.prerelease_suffix)
      end


      # Test an arbitrary value.

      def test_arbitrary_value
        value_ = ::Versionomy.create([1, 9, 2, 'pre2'], :semver)
        assert_equal(1, value_.major)
        assert_equal(9, value_.minor)
        assert_equal(2, value_.patch)
        assert_equal('pre2', value_.prerelease_suffix)
      end


      # Test aliases

      def test_alias_fields
        value_ = ::Versionomy.create([1, 9, 2, 'pre2'], :semver)
        assert_equal('pre2', value_.special_suffix)
      end


      # Test construction using aliases

      def test_alias_field_construction
        value_ = ::Versionomy.create({:major => 1, :minor => 9, :special_suffix => 'pre2'}, :semver)
        assert_equal([1, 9, 0, 'pre2'], value_.values_array)
      end


      # Test comparison of numeric values.

      def test_numeric_comparison
        value1_ = ::Versionomy.create([1, 9, 2], :semver)
        value2_ = ::Versionomy.create([1, 9], :semver)
        assert(value2_ < value1_)
        value1_ = ::Versionomy.create([1, 9, 0], :semver)
        value2_ = ::Versionomy.create([1, 9], :semver)
        assert(value2_ == value1_)
      end


      # Test comparison of string values.

      def test_string_comparison
        value1_ = ::Versionomy.create([1, 9, 2, 'a2'], :semver)
        value2_ = ::Versionomy.create([1, 9, 2, 'b1'], :semver)
        assert(value2_ > value1_)
      end


      # Test comparison of numeric and string values.

      def test_numeric_and_string_comparison
        value1_ = ::Versionomy.create([1, 9, 2, 'a2'], :semver)
        value2_ = ::Versionomy.create([1, 9, 2], :semver)
        assert(value2_ > value1_)
      end


      # Test parsing numeric.

      def test_parsing_numeric
        value_ = ::Versionomy.parse('2.0.1', :semver)
        assert_equal([2, 0, 1, ''], value_.values_array)
        assert_equal('2.0.1', value_.unparse)
      end


      # Test parsing with a string.

      def test_parsing_with_string
        value_ = ::Versionomy.parse('1.9.2pre2', :semver)
        assert_equal([1, 9, 2, 'pre2'], value_.values_array)
        assert_equal('1.9.2pre2', value_.unparse)
      end


      # Test making sure unparsing requires all three fields.

      def test_parsing_require_all_fields
        value_ = ::Versionomy.parse('2.0', :semver)
        assert_equal([2, 0, 0, ''], value_.values_array)
        assert_equal('2.0.0', value_.unparse)
        assert_raises(::Versionomy::Errors::ParseError) do
          value_ = ::Versionomy.parse('2.0b1', :semver)
        end
      end


      # Test convenience parsing

      def test_convenience_parse
        value_ = ::Versionomy.semver('2.0.1')
        assert_equal([2, 0, 1, ''], value_.values_array)
      end


      # Test convenience creation from hash

      def test_convenience_create
        value_ = ::Versionomy.semver(:major => 2, :patch => 1, :prerelease_suffix => 'b2')
        assert_equal([2, 0, 1, 'b2'], value_.values_array)
      end


      # Test bumping a numeric field.

      def test_bump_numeric
        value_ = ::Versionomy.create([1, 9, 2, 'a2'], :semver)
        value_ = value_.bump(:patch)
        assert_equal([1, 9, 3, ''], value_.values_array)
      end


      # Test bumping a string field.

      def test_bump_string
        value_ = ::Versionomy.create([1, 9, 2, 'a2'], :semver)
        value_ = value_.bump(:prerelease_suffix)
        assert_equal([1, 9, 2, 'a3'], value_.values_array)
      end


      # Test "prerelase?" custom method

      def test_method_prereleasep
        value_ = ::Versionomy.create([1, 9, 2, 'a2'], :semver)
        assert_equal(true, value_.prerelease?)
        value_ = ::Versionomy.create([1, 9, 2], :semver)
        assert_equal(false, value_.prerelease?)
      end


      # Test marshalling

      def test_marshal
        value_ = ::Versionomy.create([1, 9, 2, 'pre2'], :semver)
        str_ = ::Marshal.dump(value_)
        value2_ = ::Marshal.load(str_)
        assert_equal(value_, value2_)
      end


      # Test YAML

      def test_yaml
        value_ = ::Versionomy.create([1, 9, 2, 'pre2'], :semver)
        str_ = ::YAML.dump(value_)
        value2_ = ::YAML.load(str_)
        assert_equal(value_, value2_)
      end


    end

  end
end
