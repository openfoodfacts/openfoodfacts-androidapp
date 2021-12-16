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

    class TestStandardReset < ::Minitest::Test  # :nodoc:


      # Test resetting a minor patchlevel.

      def test_reset_patchlevel_minor
        value_ = ::Versionomy.create(:major => 2, :tiny => 1, :patchlevel => 3, :patchlevel_minor => 1)
        value_ = value_.reset(:patchlevel_minor)
        assert_equal([2,0,1,0,:final,3,0], value_.values_array)
      end


      # Test resetting a major patchlevel.

      def test_reset_patchlevel
        value_ = ::Versionomy.create(:major => 2, :tiny => 1, :patchlevel => 3, :patchlevel_minor => 1)
        value_ = value_.reset(:patchlevel)
        assert_equal([2,0,1,0,:final,0,0], value_.values_array)
      end


      # Test resetting a beta release type.

      def test_reset_beta_release_type
        value_ = ::Versionomy.create(:major => 2, :tiny => 1, :release_type => :beta, :beta_version => 2)
        value_ = value_.reset(:release_type)
        assert_equal([2,0,1,0,:final,0,0], value_.values_array)
      end


      # Test resetting a final release type.

      def test_reset_final_release_type
        value_ = ::Versionomy.create(:major => 2, :tiny => 1, :patchlevel => 2)
        value_ = value_.reset(:release_type)
        assert_equal([2,0,1,0,:final,0,0], value_.values_array)
      end


      # Test resetting tiny.

      def test_reset_tiny
        value_ = ::Versionomy.create(:major => 2, :tiny => 1, :tiny2 => 3, :release_type => :release_candidate, :release_candidate_version => 2)
        value_ = value_.reset(:tiny)
        assert_equal([2,0,0,0,:final,0,0], value_.values_array)
      end


      # Test resetting major.

      def test_reset_major
        value_ = ::Versionomy.create(:major => 2, :tiny => 1, :tiny2 => 3, :release_type => :release_candidate, :release_candidate_version => 2)
        value_ = value_.reset(:major)
        assert_equal([1,0,0,0,:final,0,0], value_.values_array)
      end


    end

  end
end
