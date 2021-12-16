# -----------------------------------------------------------------------------
#
# Versionomy parsing tests on standard schema
#
# This file contains tests for parsing on the standard schema
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

    class TestStandardParse < ::Minitest::Test  # :nodoc:


      # Test parsing full.

      def test_parsing_full_release
        value_ = ::Versionomy.parse('2.0.1.1-4.6')
        assert_equal(2, value_.major)
        assert_equal(0, value_.minor)
        assert_equal(1, value_.tiny)
        assert_equal(1, value_.tiny2)
        assert_equal(:final, value_.release_type)
        assert_equal(4, value_.patchlevel)
        assert_equal(6, value_.patchlevel_minor)
        assert_equal('2.0.1.1-4.6', value_.unparse)
      end


      # Test parsing abbreviated.

      def test_parsing_abbrev_release
        value_ = ::Versionomy.parse('2.0.1')
        assert_equal(2, value_.major)
        assert_equal(0, value_.minor)
        assert_equal(1, value_.tiny)
        assert_equal(0, value_.tiny2)
        assert_equal(:final, value_.release_type)
        assert_equal(0, value_.patchlevel)
        assert_equal(0, value_.patchlevel_minor)
        assert_equal('2.0.1.0-0.0', value_.unparse(:required_fields => [:minor, :tiny, :tiny2, :patchlevel, :patchlevel_minor]))
        assert_equal('2.0.1-0', value_.unparse(:required_fields => [:minor, :patchlevel]))
        assert_equal('2.0.1', value_.unparse)
      end


      # Test parsing with trailing zeros.

      def test_parsing_trailing_zeros
        value_ = ::Versionomy.parse('2.0.0')
        assert_equal(2, value_.major)
        assert_equal(0, value_.minor)
        assert_equal(0, value_.tiny)
        assert_equal(0, value_.tiny2)
        assert_equal(:final, value_.release_type)
        assert_equal(0, value_.patchlevel)
        assert_equal(0, value_.patchlevel_minor)
        assert_equal('2.0.0', value_.unparse)
      end


      # Test parsing with leading zeros on a field.

      def test_parsing_field_leading_zeros
        value_ = ::Versionomy.parse('2.09')
        assert_equal(2, value_.major)
        assert_equal(9, value_.minor)
        assert_equal(0, value_.tiny)
        assert_equal('2.09', value_.unparse)
        value_ = value_.bump(:minor)
        assert_equal(10, value_.minor)
        assert_equal('2.10', value_.unparse)
        value_ = value_.change(:minor => 123)
        assert_equal(123, value_.minor)
        assert_equal('2.123', value_.unparse)
        value_ = value_.change(:minor => 4)
        assert_equal(4, value_.minor)
        assert_equal('2.04', value_.unparse)
        value_ = ::Versionomy.parse('2.00')
        assert_equal(0, value_.minor)
        assert_equal('2.00', value_.unparse)
      end


      # Test unparsing with a minimum width.

      def test_unparsing_minimum_width_field
        value_ = ::Versionomy.parse('2.0')
        assert_equal('2.0', value_.unparse)
        assert_equal('2.00', value_.unparse(:minor_width => 2))
        assert_equal('02.0', value_.unparse(:major_width => 2))
        value_ = value_.bump(:minor)
        assert_equal('2.1', value_.unparse)
        assert_equal('2.01', value_.unparse(:minor_width => 2))
        value_ = value_.change(:minor => 12)
        assert_equal('2.12', value_.unparse)
        assert_equal('2.12', value_.unparse(:minor_width => 1))
      end


      # Test parsing major version only.

      def test_parsing_major_only
        value_ = ::Versionomy.parse('2')
        assert_equal(2, value_.major)
        assert_equal(0, value_.minor)
        assert_equal(0, value_.tiny)
        assert_equal(0, value_.tiny2)
        assert_equal(:final, value_.release_type)
        assert_equal(0, value_.patchlevel)
        assert_equal(0, value_.patchlevel_minor)
        assert_equal('2', value_.unparse)
      end


      # Test parsing preview.

      def test_parsing_preview
        value_ = ::Versionomy.parse('2.0.1pre4')
        assert_equal(2, value_.major)
        assert_equal(0, value_.minor)
        assert_equal(1, value_.tiny)
        assert_equal(0, value_.tiny2)
        assert_equal(:preview, value_.release_type)
        assert_equal(4, value_.preview_version)
        assert_equal(0, value_.preview_minor)
        assert_equal('2.0.1pre4', value_.unparse)
        assert_equal('2.0.1pre4.0', value_.unparse(:required_fields => [:preview_minor]))
      end


      # Test parsing alpha.

      def test_parsing_alpha
        value_ = ::Versionomy.parse('2.0.1a4.1')
        assert_equal(2, value_.major)
        assert_equal(0, value_.minor)
        assert_equal(1, value_.tiny)
        assert_equal(0, value_.tiny2)
        assert_equal(:alpha, value_.release_type)
        assert_equal(4, value_.alpha_version)
        assert_equal(1, value_.alpha_minor)
        assert_equal('2.0.1a4.1', value_.unparse)
        assert_equal('2.0.1a4.1', value_.unparse(:optional_fields => [:alpha_minor]))
      end


      # Test parsing beta.

      def test_parsing_beta
        value_ = ::Versionomy.parse('2.52.1b4.0')
        assert_equal(2, value_.major)
        assert_equal(52, value_.minor)
        assert_equal(1, value_.tiny)
        assert_equal(0, value_.tiny2)
        assert_equal(:beta, value_.release_type)
        assert_equal(4, value_.beta_version)
        assert_equal(0, value_.beta_minor)
        assert_equal('2.52.1b4.0', value_.unparse)
        assert_equal('2.52.1b4', value_.unparse(:optional_fields => [:beta_minor]))
      end


      # Test parsing beta alternates

      def test_parsing_beta_alternates
        assert_equal(::Versionomy.parse('2.52.1 beta4'), '2.52.1b4')
        assert_equal(::Versionomy.parse('2.52.1-b4'), '2.52.1b4')
        assert_equal(::Versionomy.parse('2.52.1_b4'), '2.52.1b4')
        assert_equal(::Versionomy.parse('2.52.1.b4'), '2.52.1b4')
        assert_equal(::Versionomy.parse('2.52.1B4'), '2.52.1b4')
        assert_equal(::Versionomy.parse('2.52.1BETA4'), '2.52.1b4')
        assert_equal(::Versionomy.parse('2.52.1 Beta4'), '2.52.1b4')
        assert_equal(::Versionomy.parse('2.52.1 eta4', :extra_characters => :ignore), '2.52.1')
        assert_equal(::Versionomy.parse('2.52.1 Beta'), '2.52.1b0')
      end


      # Test parsing release candidate.

      def test_parsing_release_candidate
        value_ = ::Versionomy.parse('0.2rc0')
        assert_equal(0, value_.major)
        assert_equal(2, value_.minor)
        assert_equal(0, value_.tiny)
        assert_equal(0, value_.tiny2)
        assert_equal(:release_candidate, value_.release_type)
        assert_equal(0, value_.release_candidate_version)
        assert_equal(0, value_.release_candidate_minor)
        assert_equal('0.2rc0', value_.unparse)
        assert_equal('0.2rc0.0', value_.unparse(:required_fields => [:release_candidate_minor]))
        assert_equal('0.2rc', value_.unparse(:optional_fields => [:release_candidate_version]))
      end


      # Test parsing release candidate changing to other prerelease.
      # Ensures that :short style takes precedence over :long for parsing "rc".

      def test_parsing_release_candidate_type_change
        value_ = ::Versionomy.parse('0.2rc1')
        assert_equal(:release_candidate, value_.release_type)
        assert_equal(1, value_.release_candidate_version)
        assert_equal('0.2rc1', value_.unparse)
        value_ = value_.change(:release_type => :beta)
        assert_equal(:beta, value_.release_type)
        assert_equal(1, value_.beta_version)
        assert_equal('0.2b1', value_.unparse)
        value_ = value_.change({:release_type => :beta}, :release_type_style => :long)
        assert_equal(:beta, value_.release_type)
        assert_equal(1, value_.beta_version)
        assert_equal('0.2beta1', value_.unparse)
      end


      # Test parsing forms without a prerelease version

      def test_parsing_without_prerelease_version
        value_ = ::Versionomy.parse('1.9.2dev')
        assert_equal(value_.release_type, :development)
        assert_equal(value_.development_version, 0)
        assert_equal('1.9.2dev', value_.to_s)
        value_ = value_.bump(:development_version)
        assert_equal('1.9.2dev1', value_.to_s)
      end


      # Test parsing forms without a prerelease version.
      # Ensures that :development_version prefers to be required.

      def test_unparsing_prerelease_version_0
        value_ = ::Versionomy.parse('1.9.2dev1')
        assert_equal(value_.release_type, :development)
        assert_equal(value_.development_version, 1)
        assert_equal('1.9.2dev1', value_.to_s)
        value2_ = value_.change(:development_version => 0)
        assert_equal('1.9.2dev0', value2_.to_s)
        value2_ = value_.change({:development_version => 0}, :optional_fields => [:development_version])
        assert_equal('1.9.2dev', value2_.to_s)
      end


      # Test unparsing a value that requires lookback.

      def test_unparsing_with_lookback
        value_ = ::Versionomy.parse('2.0')
        value2_ = value_.change(:tiny2 => 1)
        assert_equal(1, value2_.tiny2)
        assert_equal('2.0.0.1', value2_.unparse)
        value3_ = value2_.change(:tiny2 => 0)
        assert_equal(0, value3_.tiny2)
        assert_equal('2.0', value3_.unparse)
      end


      # Test delimiter changes in a multi-form field.

      def test_multi_form_delimiter_changes
        value_ = ::Versionomy.parse('2.0 preview 1')
        assert_equal('2.0 preview 1', value_.to_s)
        value2_ = value_.change(:release_type => :final)
        assert_equal('2.0', value2_.to_s)
        value3_ = value2_.change(:release_type => :preview, :preview_version => 1)
        assert_equal('2.0 preview 1', value3_.to_s)
      end


      # Test different patchlevel separators.

      def test_patchlevel_separators
        expected_ = [1,9,1,0,:final,243,0]
        assert_equal(expected_, ::Versionomy.parse('1.9.1-p243').values_array)
        assert_equal(expected_, ::Versionomy.parse('1.9.1_p243').values_array)
        assert_equal(expected_, ::Versionomy.parse('1.9.1_u243').values_array)
        assert_equal(expected_, ::Versionomy.parse('1.9.1p243').values_array)
        assert_equal(expected_, ::Versionomy.parse('1.9.1.p243').values_array)
        assert_equal(expected_, ::Versionomy.parse('1.9.1 p243').values_array)
        assert_equal(expected_, ::Versionomy.parse('1.9.1-243').values_array)
        assert_equal(expected_, ::Versionomy.parse('1.9.1_243').values_array)
      end


      # Test alphabetic patchlevels.
      # In particular, make sure the parser can distinguish between these
      # and the markers for prereleases.

      def test_patchlevel_alphabetic
        value_ = ::Versionomy.parse('1.9a')
        assert_equal([1, 9, 0, 0, :final, 1, 0], value_.values_array)
        assert_equal('1.9a', value_.to_s)
        value_ = ::Versionomy.parse('1.9b')
        assert_equal([1, 9, 0, 0, :final, 2, 0], value_.values_array)
        assert_equal('1.9b', value_.to_s)
        value_ = ::Versionomy.parse('1.9d')
        assert_equal([1, 9, 0, 0, :final, 4, 0], value_.values_array)
        assert_equal('1.9d', value_.to_s)
        value_ = ::Versionomy.parse('1.9p')
        assert_equal([1, 9, 0, 0, :final, 16, 0], value_.values_array)
        assert_equal('1.9p', value_.to_s)
        value_ = ::Versionomy.parse('1.9r')
        assert_equal([1, 9, 0, 0, :final, 18, 0], value_.values_array)
        assert_equal('1.9r', value_.to_s)
        value_ = ::Versionomy.parse('1.9u')
        assert_equal([1, 9, 0, 0, :final, 21, 0], value_.values_array)
        assert_equal('1.9u', value_.to_s)
      end


      # Test setting delimiters on unparse, including testing for illegal delimiters

      def test_unparse_with_custom_delimiters
        value_ = ::Versionomy.parse('1.2b3')
        assert_equal('1.2.b.3', value_.unparse(:release_type_delim => '.', :beta_version_delim => '.'))
        assert_equal('1.2b3', value_.unparse(:release_type_delim => '=', :beta_version_delim => '*'))
        value_ = ::Versionomy.parse('1.2-4')
        assert_equal('1.2-4', value_.unparse(:release_type_delim => '.'))
      end


      # Test java version formats

      def test_java_formats
        value_ = ::Versionomy.parse('1.6.0_17')
        assert_equal([1, 6, 0, 0, :final, 17, 0], value_.values_array)
        assert_equal('1.6.0_17', value_.to_s)
        value_ = ::Versionomy.parse('6u17')
        assert_equal([6, 0, 0, 0, :final, 17, 0], value_.values_array)
        assert_equal('6u17', value_.to_s)
      end


      # Test formats prefixed with "v"

      def test_v_prefix
        value_ = ::Versionomy.parse('v1.2')
        assert_equal([1, 2, 0, 0, :final, 0, 0], value_.values_array)
        assert_equal('v1.2', value_.to_s)
        value_ = ::Versionomy.parse('V 2.3')
        assert_equal([2, 3, 0, 0, :final, 0, 0], value_.values_array)
        assert_equal('V 2.3', value_.to_s)
      end


      # Test parse errors

      def test_parsing_errors
        assert_raises(::Versionomy::Errors::ParseError) do
          ::Versionomy.parse('2.52.1 eta4')
        end
      end


    end

  end
end
