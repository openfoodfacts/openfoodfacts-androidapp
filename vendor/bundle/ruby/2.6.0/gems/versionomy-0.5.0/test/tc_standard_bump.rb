# -----------------------------------------------------------------------------
#
# Versionomy bump tests on standard schema
#
# This file contains tests for the bump function on the standard schema
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

    class TestStandardBump < ::Minitest::Test  # :nodoc:


      # Test bumping a minor patchlevel.

      def test_bump_patchlevel_minor
        value_ = ::Versionomy.create(:major => 2, :tiny => 1, :patchlevel => 3, :patchlevel_minor => 0)
        value_ = value_.bump(:patchlevel_minor)
        assert_equal(2, value_.major)
        assert_equal(0, value_.minor)
        assert_equal(1, value_.tiny)
        assert_equal(0, value_.tiny2)
        assert_equal(:final, value_.release_type)
        assert_equal(3, value_.patchlevel)
        assert_equal(1, value_.patchlevel_minor)
      end


      # Test bumping a major patchlevel.

      def test_bump_patchlevel
        value_ = ::Versionomy.create(:major => 2, :tiny => 1, :patchlevel => 3, :patchlevel_minor => 1)
        value_ = value_.bump(:patchlevel)
        assert_equal(2, value_.major)
        assert_equal(0, value_.minor)
        assert_equal(1, value_.tiny)
        assert_equal(0, value_.tiny2)
        assert_equal(:final, value_.release_type)
        assert_equal(4, value_.patchlevel)
        assert_equal(0, value_.patchlevel_minor)
      end


      # Test bumping release type preview.

      def test_bump_preview_to_release
        value_ = ::Versionomy.create(:major => 2, :tiny => 1, :release_type => :preview)
        value_ = value_.bump(:release_type)
        assert_equal(2, value_.major)
        assert_equal(0, value_.minor)
        assert_equal(1, value_.tiny)
        assert_equal(0, value_.tiny2)
        assert_equal(:final, value_.release_type)
        assert_equal(0, value_.patchlevel)
        assert_equal(0, value_.patchlevel_minor)
      end


      # Test bumping release type development.

      def test_bump_development_to_alpha
        value_ = ::Versionomy.create(:major => 2, :tiny => 1, :release_type => :development, :development_version => 7)
        value_ = value_.bump(:release_type)
        assert_equal(2, value_.major)
        assert_equal(0, value_.minor)
        assert_equal(1, value_.tiny)
        assert_equal(0, value_.tiny2)
        assert_equal(:alpha, value_.release_type)
        assert_equal(1, value_.alpha_version)
        assert_equal(0, value_.alpha_minor)
      end


      # Test bumping release type alpha.

      def test_bump_alpha_to_beta
        value_ = ::Versionomy.create(:major => 2, :tiny => 1, :release_type => :alpha)
        value_ = value_.bump(:release_type)
        assert_equal(2, value_.major)
        assert_equal(0, value_.minor)
        assert_equal(1, value_.tiny)
        assert_equal(0, value_.tiny2)
        assert_equal(:beta, value_.release_type)
        assert_equal(1, value_.beta_version)
        assert_equal(0, value_.beta_minor)
      end


      # Test bumping release type release_candidate.

      def test_bump_release_candidate_to_release
        value_ = ::Versionomy.create(:major => 2, :tiny => 1, :release_type => :release_candidate, :release_candidate_version => 2)
        value_ = value_.bump(:release_type)
        assert_equal(2, value_.major)
        assert_equal(0, value_.minor)
        assert_equal(1, value_.tiny)
        assert_equal(0, value_.tiny2)
        assert_equal(:final, value_.release_type)
        assert_equal(0, value_.patchlevel)
        assert_equal(0, value_.patchlevel_minor)
      end


      # Test bumping tiny.

      def test_bump_tiny
        value_ = ::Versionomy.create(:major => 2, :tiny => 1, :tiny2 => 3, :release_type => :release_candidate, :release_candidate_version => 2)
        value_ = value_.bump(:tiny)
        assert_equal(2, value_.major)
        assert_equal(0, value_.minor)
        assert_equal(2, value_.tiny)
        assert_equal(0, value_.tiny2)
        assert_equal(:final, value_.release_type)
        assert_equal(0, value_.patchlevel)
        assert_equal(0, value_.patchlevel_minor)
      end


      # Test bumping major.

      def test_bump_major
        value_ = ::Versionomy.create(:major => 2, :tiny => 1, :tiny2 => 3, :release_type => :release_candidate, :release_candidate_version => 2)
        value_ = value_.bump(:major)
        assert_equal(3, value_.major)
        assert_equal(0, value_.minor)
        assert_equal(0, value_.tiny)
        assert_equal(0, value_.tiny2)
        assert_equal(:final, value_.release_type)
        assert_equal(0, value_.patchlevel)
        assert_equal(0, value_.patchlevel_minor)
      end


    end

  end
end
