# -----------------------------------------------------------------------------
#
# Versionomy comparison tests on standard schema
#
# This file contains tests for comparisons on the standard schema
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

    class TestStandardComparison < ::Minitest::Test  # :nodoc:


      # Test comparisons with difference in major.

      def test_comparison_major
        value1_ = ::Versionomy.create(:major => 2, :tiny => 1, :tiny2 => 3, :release_type => :release_candidate, :release_candidate_version => 2)
        value2_ = ::Versionomy.create(:major => 3, :release_type => :alpha)
        assert(value2_ > value1_)
      end


      # Test comparisons with difference in minor.

      def test_comparison_minor
        value1_ = ::Versionomy.create(:major => 2, :tiny => 1, :tiny2 => 3, :release_type => :release_candidate, :release_candidate_version => 2)
        value2_ = ::Versionomy.create(:major => 2, :minor => 1, :release_type => :alpha)
        assert(value2_ > value1_)
      end


      # Test comparisons with difference in release type.

      def test_comparison_release_type
        value1_ = ::Versionomy.create(:major => 2, :release_type => :alpha, :alpha_version => 5)
        value2_ = ::Versionomy.create(:major => 2, :release_type => :release_candidate, :release_candidate_version => 2)
        assert(value2_ > value1_)
      end


      # Test equality for a simple case.

      def test_equality_simple
        value1_ = ::Versionomy.create(:major => 2, :minor => 0, :release_type => :alpha, :alpha_version => 5)
        value2_ = ::Versionomy.create(:major => 2, :release_type => :alpha, :alpha_version => 5)
        assert_equal(value2_, value1_)
        assert_equal(value2_.hash, value1_.hash)
      end


      # Test equality from parsed values.

      def test_equality_parsed
        value1_ = ::Versionomy.parse("1.8.7p72")
        value2_ = ::Versionomy.parse("1.8.7.0-72.0")
        assert_equal(value2_, value1_)
        assert_equal(value2_.hash, value1_.hash)
      end


      # Test non-equality from parsed values.

      def test_nonequality_parsed
        value1_ = ::Versionomy.parse("1.8.7b7")
        value2_ = ::Versionomy.parse("1.8.7a7")
        refute_equal(value2_, value1_)
        refute_equal(value2_.hash, value1_.hash)
      end


      # Test equality with string.

      def test_equality_string
        value1_ = ::Versionomy.parse("1.8.7p72")
        assert_operator(value1_, :==, "1.8.7p72")
        assert_operator(value1_, :==, "1.8.7.0-72.0")
      end


      # Test comparison with string.

      def test_comparison_string
        value1_ = ::Versionomy.parse("1.8.7p72")
        assert_operator(value1_, :<, "1.8.7p73")
        assert_operator(value1_, :<, "1.8.8pre1")
        assert_operator(value1_, :>, "1.8.7p71")
        assert_operator(value1_, :>, "1.8.7rc2")
        assert_operator(value1_, :>, "1.8.7.0")
      end


      # Test sorting.

      def test_sort
        value1_ = ::Versionomy.parse("1.8.7p73")
        value2_ = ::Versionomy.parse("1.8.7p72")
        value3_ = ::Versionomy.parse("1.8.8pre1")
        value4_ = ::Versionomy.parse("1.8.7.0")
        value5_ = ::Versionomy.parse("1.8.7rc2")
        assert_equal([value5_, value4_, value2_, value1_, value3_],
                     [value1_, value2_, value3_, value4_, value5_].sort)
      end


    end

  end
end
