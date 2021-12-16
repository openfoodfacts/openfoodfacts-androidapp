# -----------------------------------------------------------------------------
#
# Versionomy change tests on standard schema
#
# This file contains tests for the change function on the standard schema
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

    class TestStandardChange < ::Minitest::Test  # :nodoc:


      # Test with a changed tiny

      def test_change_tiny
        value_ = ::Versionomy.create(:major => 2, :tiny => 1, :tiny2 => 3, :release_type => :release_candidate, :release_candidate_version => 2)
        value_ = value_.change(:tiny => 4)
        assert_equal(2, value_.major)
        assert_equal(0, value_.minor)
        assert_equal(4, value_.tiny)
        assert_equal(3, value_.tiny2)
        assert_equal(:release_candidate, value_.release_type)
        assert_equal(2, value_.release_candidate_version)
        assert_equal(0, value_.release_candidate_minor)
      end


      # Test with several changed fields

      def test_change_several
        value_ = ::Versionomy.create(:major => 2, :tiny => 1, :tiny2 => 3, :release_type => :release_candidate, :release_candidate_version => 2)
        value_ = value_.change(:tiny => 4, :release_candidate_version => 5)
        assert_equal(2, value_.major)
        assert_equal(0, value_.minor)
        assert_equal(4, value_.tiny)
        assert_equal(3, value_.tiny2)
        assert_equal(:release_candidate, value_.release_type)
        assert_equal(5, value_.release_candidate_version)
        assert_equal(0, value_.release_candidate_minor)
      end


      # Test with a changed release type

      def test_change_release_type
        value_ = ::Versionomy.create(:major => 2, :tiny => 1, :tiny2 => 3, :release_type => :release_candidate, :release_candidate_version => 2)
        value_ = value_.change(:release_type => :beta)
        assert_equal(2, value_.major)
        assert_equal(0, value_.minor)
        assert_equal(1, value_.tiny)
        assert_equal(3, value_.tiny2)
        assert_equal(:beta, value_.release_type)
        assert_equal(1, value_.beta_version)
        assert_equal(0, value_.beta_minor)
        assert_equal(false, value_.has_field?(:release_candidate_version))
        assert_equal(false, value_.has_field?(:release_candidate_minor))
      end


    end

  end
end
